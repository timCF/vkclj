(ns vkclj.core
  (:require [clojure.data.json :as json])
  (:require [org.httpkit.client :as http])
  (:use [clojure.core.match :only (match)])
  (:use [pmclj.core :only [key_not_matching]]))

;; work with jsons
(defmacro decode [body]
  `(try
    (json/read-str ~body)
    (catch Exception ~'e {:error (str "caught exception while decoding: " (.getMessage ~'e))})))
(defmacro encode [body]
  `(try
    (json/write-str ~body)
    (catch Exception ~'e {:error (str "caught exception while encoding: " (.getMessage ~'e))})))

(defmacro get_response [some_map]
  `(let [res# (get-in ~some_map ["response"])]
    (case (= nil res#)
      true {:error ~some_map}
      res#)))

(defmacro final_handler [final_res result_symbol handler]
  `(let [~result_symbol ~final_res]
     ~handler))

(defmacro check_args [real_args key_list]
  `(every? #(contains? ~real_args %) ~key_list))

;; request template
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
