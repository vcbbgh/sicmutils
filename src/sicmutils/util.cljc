;;
;; Copyright © 2020 Sam Ritchie.
;; This work is based on the Scmutils system of MIT/GNU Scheme:
;; Copyright © 2002 Massachusetts Institute of Technology
;;
;; This is free software;  you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as published by
;; the Free Software Foundation; either version 3 of the License, or (at
;; your option) any later version.
;;
;; This software is distributed in the hope that it will be useful, but
;; WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
;; General Public License for more details.
;;
;; You should have received a copy of the GNU General Public License
;; along with this code; if not, see <http://www.gnu.org/licenses/>.
;;

(ns sicmutils.util
  "Shared utilities between clojure and clojurescript."
  (:refer-clojure :rename {bigint core-bigint
                           biginteger core-biginteger
                           int core-int
                           long core-long
                           double core-double}
                  #?@(:cljs [:exclude [bigint double long int]]))
  (:require #?(:clj [clojure.math.numeric-tower :as nt])
            #?(:cljs goog.math.Integer)
            #?(:cljs goog.math.Long))
  #?(:clj
     (:import [clojure.lang BigInt]
              [java.util.concurrent TimeUnit TimeoutException])))

(defn counted
  "Takes a function and returns a pair of:

  - an atom that keeps track of fn invocation counts,
  - the instrumented fn"
  ([f] (counted f 0))
  ([f initial-count]
   (let [count (atom initial-count)]
     [count (fn [x]
              (swap! count inc)
              (f x))])))

(defmacro import-def
  "Given a regular def'd var from another namespace, defined a new var with the
   same name in the current namespace.

  This macro is modeled after `potemkin.namespaces/import-def` but meant to be
  usable from Clojurescript. In Clojurescript, it's not possible to:

  - alter the metadata of a var after definition
  - call `resolve` at macro-time

  And therefore not possible to mirror the metadata from one var to another.
  This simplified version therefore suffices in the cljs case."
  ([sym]
   `(import-def ~sym nil))
  ([sym var-name]
   (let [n (or var-name (symbol (name sym)))]
     `(def ~n ~sym))))

(defmacro import-vars
  "import multiple defs from multiple namespaces. works for vars and fns. not
  macros.

  [[import-vars]] has the same syntax as `potemkin.namespaces/import-vars`:

   ```clojure
  (import-vars
     [m.n.ns1 a b]
     [x.y.ns2 d e f]) =>
   (def a m.n.ns1/a)
   (def b m.n.ns1/b)
    ...
   (def d m.n.ns2/d)
    ... etc
  ```"
  [& imports]
  (let [expanded-imports (for [[from-ns & defs] imports
                               d defs
                               :let [sym (symbol (str from-ns)
                                                 (str d))]]
                           `(def ~d ~sym))]
    `(do ~@expanded-imports)))

(def compute-sqrt #?(:clj nt/sqrt :cljs Math/sqrt))
(def compute-expt #?(:clj nt/expt :cljs Math/pow))
(def compute-abs #?(:clj nt/abs :cljs Math/abs))
(def biginttype #?(:clj BigInt :cljs js/BigInt))
(def inttype #?(:clj Integer :cljs goog.math.Integer))
(def longtype #?(:clj Long :cljs goog.math.Long))

(defn keyset [m]
  (into #{} (keys m)))

(defn bigint [x]
  #?(:clj (core-bigint x)
     :cljs (js/BigInt x)))

(defn ^boolean bigint?
  "Returns true if the supplied `x` is a `BigInt`, false otherwise."
  [x]
  #?(:clj (instance? BigInt x)
     :cljs (if-not (nil? x)
             (identical? (.-constructor x) js/BigInt)
             false)))

(defn parse-bigint [x]
  `(bigint ~x))

(defn biginteger [x]
  #?(:clj (core-biginteger x)
     :cljs (js/BigInt x)))

(defn int [x]
  #?(:clj (core-int x)
     :cljs (.fromNumber goog.math.Integer x)))

(defn long [x]
  #?(:clj (core-long x)
     :cljs (.fromNumber goog.math.Long x)))

(defn double [x]
  #?(:clj (core-double x)
     :cljs (if (number? x) x (js/Number x))))

(defn unsupported [s]
  (throw
   #?(:clj (UnsupportedOperationException. s)
      :cljs (js/Error s))))

(defn exception [s]
  (throw
   #?(:clj (Exception. s)
      :cljs (js/Error s))))

(defn illegal [s]
  (throw
   #?(:clj (IllegalArgumentException. s)
      :cljs (js/Error s))))

(defn illegal-state [s]
  (throw
   #?(:clj (IllegalStateException. s)
      :cljs (js/Error s))))

(defn arithmetic-ex [s]
  (throw
   #?(:clj (ArithmeticException. s)
      :cljs (js/Error s))))

(defn timeout-ex [s]
  (throw
   #?(:clj (TimeoutException. s)
      :cljs (js/Error s))))

(defn failure-to-converge [s]
  (throw
   #?(:clj (Exception. s)
      :cljs (js/Error s))))
