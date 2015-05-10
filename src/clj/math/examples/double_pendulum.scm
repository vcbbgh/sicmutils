(ns math.examples.double-pendulum
  (:refer-clojure :exclude [+ - * /])
  (:require [math.env :refer :all]
            [math.mechanics.lagrange :refer :all]
            [clojure.data.json :as json]))

(defn- T-double-pend
  [m1 m2 l1 l2 g y]
  (let [y' (D y)]
    (fn [[t [θ φ] [θdot φdot]]]
        (let [x1 ])
        )))

(defn- T-pend
  [m l _ y]
  (let [y' (D y)]
    (fn [[t θ θdot]]
      (* 1/2 m
         (+ (square (* l θdot))
            (square (y' t))
            (* 2 l (y' t) θdot (sin θ)))))))

(defn- V-pend
  [m l g y]
  (fn [[t θ _]]
    (* m g (- (y t) (* l (cos θ))))))

(def L-pend (- T-pend V-pend))

(defn periodic-drive
  [A ω φ]
  #(-> % (* ω) (+ φ) cos (* A)))

(defn pend-state-derivative  [m l g drive]
  (Lagrangian->state-derivative
   (L-pend m l g drive)))

(defn evolve-pendulum
  [t A ω g θ0 θdot0]
  (let [drive (periodic-drive A ω 0)
        state-history (atom [])]
    ((evolve pend-state-derivative
             1.0 ;; mass of bob
             1.0 ;; length of rod
             g ;; acceleration due to gravity
             drive ;; motion of pendulum support
             )
     (up 0.0
         θ0
         θdot0)
     (fn [t [_ q _]] (swap! state-history conj [t q (drive t)]))
     0.01
     t
     1.0e-13
     {:compile true})
    @state-history))

(defn -main
  [& args]
  (let [[t A ω g θ0 θdot0] (if args
                         (map #(Double/valueOf %) args)
                         [1. 0.1 (* 2.0 (sqrt 9.8)) 9.8 1. 0.])]
    (json/write (evolve-pendulum t A ω g θ0 θdot0) *out*)))