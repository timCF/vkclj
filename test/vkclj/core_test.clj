(ns vkclj.core-test
  (:require [clojure.test :refer :all]
            [vkclj.core :refer :all]))

(def token "88000e4fc461904f51fa70bdea7fe201b8ca5dcc2a21198689aec841a483545dad1fc0b5b50b706a5792d")

(deftest gettime_test
  (testing "gettime_test"
    (is (integer? (gettime)))))

(deftest getphotos_test
  (testing "getphotos"
    (is (vector? (getphotos {:owner_id -37579590 :aid 160325516 :access_token token})))))

(deftest upload_photo_test
  (testing "upload_photo_test."
    (is (= :ok (upload_photo {:aid 206579664 :photo_path "./main.jpg" :access_token token})))))