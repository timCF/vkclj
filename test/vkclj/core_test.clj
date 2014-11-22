(ns vkclj.core-test
  (:require [clojure.test :refer :all]
            [vkclj.core :refer :all]))

(def token "042c04218ead5d6a89b50df2ff6a608cd816a56c6ef9efe97b2433ff7c2380e2dbc0b41414f4d8f5a606c")

(deftest gettime_test
  (testing "gettime_test"
    (is (integer? (gettime)))))

(deftest getphotos_test
  (testing "getphotos"
    (is (vector? (getphotos {:owner_id -37579590 :aid 160325516 :access_token token})))))

(deftest upload_photo_test1
  (testing "upload_photo_test1"
    (is (= :ok (upload_photo {:gid 64104734 :aid 185287475 :photo_path "./main.jpg" :caption "test" :access_token token})))))

(deftest upload_photo_test2
  (testing "upload_photo_test2"
    (is (= :ok (upload_photo {:aid 206579664 :photo_path "./main.jpg" :caption "test" :access_token token})))))