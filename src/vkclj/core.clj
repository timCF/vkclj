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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;  public API functions  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; {}
(vkreq gettime "getServerTime"
       (fn [some_int]
         (case (integer? some_int)
           true some_int
           false {:error {:from_vk some_int}})))

; {:owner_id -gid :aid aid}
(vkreq getphotos "photos.get"
       (fn [lst]
         (cond
           (vector? lst) lst
           (list? lst) (vec lst)
           :else {:error {:from_vk lst}})))
;(vkreq delphoto "photos.delete")
;(vkreq send_message "messages.send")

(def token "29888b63ab3627919a4e3f27eb6abf81fde006575dcd5d65b944626585503db1737288c8fbd1d67f56719")

; https://oauth.vk.com/authorize?client_id=4053347&scope=notify,friends,photos,audio,video,docs,notes,pages,status,offers,questions,wall,groups,messages,notifications,stats,ads,offline&redirect_uri=http://oauth.vk.com/blank.html&display=page&response_type=token
