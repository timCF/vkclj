(ns vkclj.core
  (:require [clojure.data.json :as json])
  (:require [clj-http.client :as http])
  (:use [clojure.core.match :only (match)])
  (:use [defun :only [defun]])
  (:use [pmclj.core :only [key_not_matching rkey_not_matching]]))

;; macro for debug

(defmacro show_it [expr]
  `(let [res# ~expr]
     (println res#)
     res#))

(def token "20ae6c1b3a945bf492b985e23371ca925e7897979ce036491878edf123c0f33f5b2b245d912e6bb6f02b3")

;; work with jsons

(defmacro decode [body]
  `(try
    (json/read-str ~body :key-fn keyword)
    (catch Exception ~'e {:error (str "caught exception while decoding: " (.getMessage ~'e))})))
(defmacro encode [body]
  `(try
    (json/write-str ~body)
    (catch Exception ~'e {:error (str "caught exception while encoding: " (.getMessage ~'e))})))

;; chain macro

(defmacro get_response [some_map]
  `(let [res# (get-in ~some_map [:response])]
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
            (match (http/get ~fullhttp)
                   {:status 200 :body body#}
                      (key_not_matching :error
                                        (decode body#)
                                        (get_response)
                                        (final_handler ~result_symbol ~handler))
                   answer# {:error {:http_res answer#}}))
      `(defn ~funcname [real_args#] (case (check_args real_args# ~key_list) true
            (match (http/get ~fullhttp {:query-params real_args# })
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
  `(let [file# (clojure.java.io/as-file ~file_path)]
     (case (.exists file#)
       true file#
       false {:error (str "File " ~file_path " is not exist.")})))

(vkreq __getupserv__ "photos.getUploadServer" [:aid :access_token] some_map
       (let [res (get-in some_map [:upload_url])]
         (case (string? res)
           true res
           false {:error {:from_vk some_map}})))

(defn __upload_photo_process__ [{upload_url :upload_url file_content :file_content}]
  (match (http/post upload_url { :multipart [{:name "file1" :content file_content}]})
         {:status 200 :body body}
            (key_not_matching :error
                              (decode body)
                              (match {:server server :photos_list photos_list :aid aid :hash hash} {:server server :photos_list photos_list :aid aid :hash hash}
                                     some_else {:error {:from_vk some_else}}))
         answer {:error {:http_res answer}}))

(vkreq __savephotos__ "photos.save" [:aid :server :photos_list :hash :access_token :caption] lst
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
       ([{:gid gid :aid aid :photo_path photo_path :caption caption :access_token token}]
        (rkey_not_matching :error
                           {:file_content (read_file photo_path)}
                           ((fn [res] (merge res {:upload_url (__getupserv__ {:gid gid :aid aid :access_token token})})))
                           (__upload_photo_process__)
                           ((fn [res] (merge res {:access_token token :caption caption :gid gid})))
                           (__savephotos__)))
       ([{:aid aid :photo_path photo_path :caption caption :access_token token}]
        (rkey_not_matching :error
                           {:file_content (read_file photo_path)}
                           ((fn [res] (merge res {:upload_url (__getupserv__ {:aid aid :access_token token})})))
                           (__upload_photo_process__)
                           ((fn [res] (merge res {:access_token token :caption caption})))
                           (__savephotos__))))
(vkreq get_friends_uids "friends.get" [:uid :access_token] lst
       (case (vector? lst)
         true (case (every? #(integer? %) lst)
                true lst
                false {:error {:from_vk lst}})
         false {:error {:from_vk lst}}))

;;
;; for inner usage
;;

(vkreq __get_group_members_proc__ "groups.getMembers" [:gid :count :offset :access_token] some_map
       (case (map? some_map)
         false {:error {:from_vk some_map}}
         true (let [users (:users some_map)]
                (case (vector? users)
                  false {:error {:from_vk some_map}}
                  true (case (every? #(integer? %) users)
                         false {:error {:from_vk some_map}}
                         true users)))))
(def get_members_limit 1000)
(defun get_group_members
       ([{:gid gid :access_token access_token :res prevres}]
         (Thread/sleep 100)
         (let [result (__get_group_members_proc__ {:gid gid
                                                   :count get_members_limit
                                                   :offset (count prevres)
                                                   :access_token access_token})]
           (match result
                  [] prevres
                  (_ :guard vector?) (get_group_members {:gid gid
                                                         :access_token access_token
                                                         :res (vec (concat prevres result))})
                  _ result)))
       ([{:gid gid :access_token access_token}]
         (get_group_members {:gid gid
                             :access_token access_token
                             :res []})))