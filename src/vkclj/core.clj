(ns vkclj.core
  (:require [clojure.data.json :as json])
  (:require [org.httpkit.client :as http])
  (:use [clojure.core.match :only (match)])
  (:use [defun :only [defun]])
  (:use [pmclj.core :only [key_not_matching pred_matching]]))

;; work with jsons

(defmacro decode [body]
  `(try
    (json/read-str ~body)
    (catch Exception ~'e {:error (str "caught exception while decoding: " (.getMessage ~'e))})))
(defmacro encode [body]
  `(try
    (json/write-str ~body)
    (catch Exception ~'e {:error (str "caught exception while encoding: " (.getMessage ~'e))})))

;; chain macro

(defmacro get_response [some_map]
  `(let [res# (get-in ~some_map ["response"])]
    (case (= nil res#)
      true {:error ~some_map}
      res#)))

(defmacro final_handler [final_res result_symbol handler]
  `(let [~result_symbol ~final_res]
     ~handler))

;; for request w args (almost all)

(defmacro check_args [real_args key_list]
  `(every? #(contains? ~real_args %) ~key_list))

;;
;; request template
;;

(defmacro vkreq [funcname method key_list result_symbol handler]
  (let [fullhttp (str "https://api.vk.com/method/" method)]
    (case key_list
      [] `(defn ~funcname []
            (match @(http/get ~fullhttp)
                   {:status 200 :body body#}
                      (key_not_matching :error
                                        (decode body#)
                                        (get_response)
                                        (final_handler ~result_symbol ~handler))
                   answer# {:error {:http_res answer#}}))
      `(defn ~funcname [real_args#] (case (check_args real_args# ~key_list) true
            (match @(http/get ~fullhttp {:query-params real_args#})
                   {:status 200 :body body#}
                      (key_not_matching :error
                                        (decode body#)
                                        (get_response)
                                        (final_handler ~result_symbol ~handler))
                   answer# {:error {:http_res answer#}}))))))

;;
;; some inner funcs for uploading photos
;;

(defmacro read_file [file_path]
  `(try
     (slurp ~file_path)
     (catch Exception e# {:error (str "caught exception while reading file " ~file_path (.getMessage e#))})))

(vkreq __getupserv__ "photos.getUploadServer" [:aid :access_token] some_map
       (let [res (get-in some_map ["upload_url"])]
         (case (string? res)
           true res
           false {:error {:from_vk some_map}})))

(defn __upload_photo_process__ [{upload_url :upload_url file_content :file_content}]
  (match @(http/post upload_url {:headers {"Content-Type" "multipart/form-data"} :multipart {:name "file1" :content file_content}})
         {:status 200 :body body}
            (key_not_matching :error
                              (decode body)
                              (get_response)
                              (match {:server server :photos_list photos_list :aid aid :hash hash} {:server server :photos_list photos_list :aid aid :hash hash}
                                     some_else {:error {:from_vk some_else}}))
         answer {:error {:http_res answer}}))

(vkreq __savephotos__ "photos.save" [:aid :server :photos_list :hash :access_token] lst
       (case (or (list? lst) (vector? lst))
         true :ok
         false {:error {:from_vk lst}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;  public API functions  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(vkreq gettime "getServerTime" [] some_int
       (case (integer? some_int)
         true some_int
         false {:error {:from_vk some_int}}))

(vkreq getphotos "photos.get" [:owner_id :aid :access_token] lst
       (cond
         (vector? lst) lst
         (list? lst) (vec lst)
         :else {:error {:from_vk lst}}))

(vkreq delphoto "photos.delete" [:oid :pid :access_token] some_int
       (case (= some_int 1)
         true :ok
         false {:error {:from_vk some_int}}))

(vkreq send_message "messages.send" [:uid :message :access_token] some_int
       (case (integer? some_int)
         true :ok
         false {:error {:from_vk some_int}}))

(defun upload_photo
       ([{:gid gid :aid aid :photo_path photo_path :access_token token}]
        (pred_matching (fn [resmap] (->> (vals resmap) (every? #(= nil (get-in % [:error])))))
                       {:file_content (read_file photo_path)}
                       (fn [res] (merge res {:upload_url (__getupserv__ {:gid gid :aid aid :access_token token})}))
                       (__upload_photo_process__)
                       (__savephotos__)))
       ([{:aid aid :photo_path photo_path :access_token token}]
        (pred_matching (fn [resmap] (->> (vals resmap) (every? #(= nil (get-in % [:error])))))
                       {:file_content (read_file photo_path)}
                       (fn [res] (merge res {:upload_url (__getupserv__ {:aid aid :access_token token})}))
                       (__upload_photo_process__)
                       (__savephotos__))))