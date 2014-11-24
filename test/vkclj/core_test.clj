(ns vkclj.core-test
  (:require [clojure.test :refer :all]
            [vkclj.core :refer :all]))

(def token "920483b0e2c68b0bba6d5865939ac1eb249a3b421060e901fe89eb0b2aeaa1722629ad1d03730f3a7cc16")
(def pid_to_del1 "344889762")
(def pid_to_del2 "318966150")

(deftest gettime_test
  (testing "gettime_test"
    (is (integer? (gettime)))))

(deftest getphotos_test
  (testing "getphotos"
    (is (vector? (getphotos {:owner_id -37579590 :aid 160325516 :access_token token})))))

(deftest delphoto_test1
  (testing "delphoto1"
    (is (= :ok (delphoto {:oid 212224431 :pid pid_to_del1 :access_token token})))))

(deftest delphoto_test2
  (testing "delphoto2"
    (is (= :ok (delphoto {:oid -64104734 :pid pid_to_del2 :access_token token})))))

(deftest send_message_test
  (testing "send_message"
    (is (= :ok (send_message {:uid 212224431 :message "Привет, мир" :access_token token})))))

(deftest upload_photo_test1
  (testing "upload_photo_test1"
    (is (= :ok (upload_photo {:gid 64104734 :aid 185287475 :photo_path "./main.jpg" :caption "привет, мир!" :access_token token})))))

(deftest upload_photo_test2
  (testing "upload_photo_test2"
    (is (= :ok (upload_photo {:aid 206579664 :photo_path "./main.jpg" :caption "привет, мир!" :access_token token})))))

