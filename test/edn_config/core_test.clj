(ns edn-config.core-test
  (:require [clojure.test :refer :all]
            [edn-config.core :refer :all]))

(deftest load-tests
  (testing "without :env anywhere"
    (is (= {:foo 123 :bar "hello"}
           (load-string "{:foo 123 :bar \"hello\"}"))))
  (testing "with matching :env at the first level"
    (is (= {:foo 123 :search-path (System/getenv "PATH") :env {:search-path :path}}
           (load-string "{:foo 123 :search-path \"placeholder\" :env {:search-path :path}}"))))
  (testing "with non-matching :env at the first level"
    (is (= {:foo 123 :search-path "placeholder" :env {:search-path :foobar-path}}
           (load-string "{:foo 123 :search-path \"placeholder\" :env {:search-path :foobar-path}}"))))
  (testing "with multiple matching :env at the first level"
    (is (= {:username (System/getenv "USER")
            :path (System/getenv "PATH")
            :env {:username :user
                  :path :path}}
           (load-string "{:username nil :path nil :env {:username :user, :path :path}}"))))
  (testing "with :env at a deeper level"
    (is (= {:foo 123
            :path (System/getenv "PATH")
            :env {:path :path}
            :bar {:baz 123
                  :username (System/getenv "USER")
                  :env {:username :user}}}
           (load-string "{:foo 123 :path nil :env {:path :path} :bar {:baz 123 :username nil :env {:username :user}}}")))))
