
(ns form-validator.validator.env
    (:refer-clojure :exclude [get-validator])
    (:require [common-state.api                  :as common-state]
              [form-validator.validator.messages :as validator.messages]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-validator
  ; @description
  ; Returns a registered form input validator.
  ;
  ; @param (keyword) validator-id
  ;
  ; @usage
  ; (reg-validator! :my-validator {:error "Please fill out this field!" :test-f #(-> % empty? not)})
  ; (get-validator  :my-validator)
  ; =>
  ; {:error "Please fill out this field!" :test-f #(-> % empty? not)}
  ;
  ; @return (map)
  ; {:error (*)
  ;  :test-f (function)
  ;  :when-changed? (boolean)
  ;  :when-left? (boolean)
  ;  :when-visited? (boolean)}
  [validator-id]
  (common-state/get-state :form-validator :validators validator-id))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn use-validator
  ; @ignore
  ;
  ; @description
  ; Returns the given validator (if provided as a map) or returns a registered validator (if provided as a keyword).
  ;
  ; @param (keyword) input-id
  ; @param (keyword or map) validator
  ;
  ; @return (map)
  ; {:error (*)
  ;  :test-f (function)
  ;  :when-changed? (boolean)
  ;  :when-left? (boolean)
  ;  :when-visited? (boolean)}
  [input-id validator]
  (letfn [(f0 [] #?(:clj  (throw (Exception. (validator.messages/missing-validator-error          input-id validator)))
                    :cljs (throw (js/Error.  (validator.messages/missing-validator-error          input-id validator)))))
          (f1 [] #?(:clj  (throw (Exception. (validator.messages/invalid-validator-type-error     input-id validator)))
                    :cljs (throw (js/Error.  (validator.messages/invalid-validator-type-error     input-id validator)))))
          (f2 [] #?(:clj  (throw (Exception. (validator.messages/invalid-validator-function-error input-id validator)))
                    :cljs (throw (js/Error.  (validator.messages/invalid-validator-function-error input-id validator)))))]
         (cond (-> validator keyword?) (if-let [% (get-validator validator)] (use-validator input-id %) (f0))
               (-> validator map?         not) (f1)
               (-> validator :test-f ifn? not) (f2)
               :return validator)))
