(ns vkclj.core
  (:require [clojure.data.json :as json])
  (:require [org.httpkit.client :as http])
  (:use [clojure.core.match :only (match)])
  (:use [defun :only [defun]]))

;; work with jsons
(defmacro decode [body]
  `(try
    (json/read-str ~body)
    (catch Exception ~'e {:error (str "caught exception while decoding: " (.getMessage ~'e))})))
(defmacro encode [body]
  `(try
    (json/write-str ~body)
    (catch Exception ~'e {:error (str "caught exception while encoding: " (.getMessage ~'e))})))

(defmacro get_response [something]
  `(get-in ~something ["response"]))

;; request template
(defmacro vkreq [funcname method handler]
  (let [fullhttp (str "https://api.vk.com/method/" method)]
    `(defun ~funcname
            ([ (~'params :guard map?) ]
             (match @(http/get ~fullhttp {:query-params ~'params})
                    {:status 200 :body ~'body}
                    (match (decode ~'body)
                           {:error ~'error} {:error ~'error}
                           ~'some (match (get_response ~'some)
                                         nil {:error ~'some}
                                         ~'ans (~handler ~'ans)))
                    ~'answer {:error {:http_res ~'answer}}))
            ([]
             (match @(http/get ~fullhttp)
                    {:status 200 :body ~'body}
                    (match (decode ~'body)
                           {:error ~'error} {:error ~'error}
                           ~'some (match (get_response ~'some)
                                         nil {:error ~'some}
                                         ~'ans (~handler ~'ans)))
                    ~'answer {:error {:http_res ~'answer}})))))

;
; next 4 funcs / macro for uploading photos
;

(defmacro read_file [file_path]
  `(try
     (slurp ~file_path)
     (catch Exception ~'e {:error (str "caught exception while reading file " ~file_path (.getMessage ~'e))})))

; {:aid aid :access_token token}
(vkreq __getupserv__ "photos.getUploadServer"
       (fn [some_map]
         (match (get-in some_map "upload_url")
                (some_str :guard string?) some_str
                some_else {:error {:from_vk some_map}})))

(defn __upload_photo_process__ [upload_url, file_content]
  (match @(http/post upload_url {:headers {"Content-Type" "multipart/form-data"} :multipart {:name "file1" :content file_content}})
         {:status 200 :body body}
         (match (decode body)
                {:error error} {:error error}
                some (match (get_response some)
                            {:server server :photos_list photos_list :aid aid :hash hash} {:server server :photos_list photos_list :aid aid :hash hash}
                            nil {:error some}))
         answer {:error {:http_res answer}}))

; {:aid aid :server server :photos_list photos_list :hash hash :access_token token}
(vkreq __savephotos__ "photos.save"
       (fn [lst]
         (case (or (list? lst) (vector? lst))
           true :ok
           false {:error {:from_vk lst}})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;  public API functions  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; {}
(vkreq gettime "getServerTime"
       (fn [some_int]
         (case (integer? some_int)
           true some_int
           false {:error {:from_vk some_int}})))

; {:owner_id -gid :aid aid :access_token token}
(vkreq getphotos "photos.get"
       (fn [lst]
         (cond
           (vector? lst) lst
           (list? lst) (vec lst)
           :else {:error {:from_vk lst}})))

; {:oid -gid :pid pid :access_token token}
(vkreq delphoto "photos.delete"
       (fn [some_int]
         (case (= some_int 1)
           true :ok
           false {:error {:from_vk some_int}})))

; {:uid uid :message message :access_token token}
(vkreq send_message "messages.send"
       (fn [some_int]
         (case (integer? some_int)
           true :ok
           false {:error {:from_vk some_int}})))

; {:gid gid :aid aid :photo_path photo_path}
(defn upload_photo [{gid :gid aid :aid photo_path :photo_path token :access_token}]
  (match (read_file photo_path)
         {:error error} {:error error}
         file_cont (match ())))
;
; TODO need write pipe matching macro
;


(def token "29888b63ab3627919a4e3f27eb6abf81fde006575dcd5d65b944626585503db1737288c8fbd1d67f56719")

; https://oauth.vk.com/authorize?client_id=4053347&scope=notify,friends,photos,audio,video,docs,notes,pages,status,offers,questions,wall,groups,messages,notifications,stats,ads,offline&redirect_uri=http://oauth.vk.com/blank.html&display=page&response_type=token
