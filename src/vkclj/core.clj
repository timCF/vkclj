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

;; request template
(defmacro vkreq [funcname method handler]
  (let [fullhttp (str "https://api.vk.com/method/" method)]
    `(defun ~funcname
            ([ (~'params :guard map?) ]
             (match @(http/get ~fullhttp {:query-params ~'params})
                    {:status 200 :body ~'body}
                    (match (decode ~'body)
                           {:error ~'error} {:error ~'error}
                           ~'some (~handler ~'some))
                    ~'answer {:error (str "query result " ~'answer)}))
            ([]
             (match @(http/get ~fullhttp)
                    {:status 200 :body ~'body}
                    (match (decode ~'body)
                           {:error ~'error} {:error ~'error}
                           ~'some (~handler ~'some))
                    ~'answer {:error (str "query result " ~'answer)})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;  public API functions  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(vkreq gettime "getServerTime"
       (fn [some_map]
         (match (get-in some_map ["response"])
                (some_int :guard integer?) some_int
                some_else {:error (str "got unexpected value " some_else)})))
;(vkreq getphotos "photos.get")
;(vkreq delphoto "photos.delete")
;(vkreq send_message "messages.send")

(def token "29888b63ab3627919a4e3f27eb6abf81fde006575dcd5d65b944626585503db1737288c8fbd1d67f56719")

; https://oauth.vk.com/authorize?client_id=4053347&scope=notify,friends,photos,audio,video,docs,notes,pages,status,offers,questions,wall,groups,messages,notifications,stats,ads,offline&redirect_uri=http://oauth.vk.com/blank.html&display=page&response_type=token
