
(ns form-validator.validator.side-effects
    (:require [common-state.api :as common-state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn reg-validator!
  ; @description
  ; Registers a reusable form input validator.
  ;
  ; @param (keyword) validator-id
  ; @param (map) validator
  ; {:error (*)
  ;  :test-f (function)
  ;  :when-changed? (boolean)(opt)
  ;   If TRUE, applies the validator when the input has been changed.
  ;  :when-left? (boolean)(opt)
  ;   If TRUE, applies the validator when the input has been left.
  ;  :when-visited? (boolean)(opt)
  ;   If TRUE, applies the validator when input has been left or changed but only if it has been left before.}
  ;
  ; @usage
  ; (reg-validator! :my-validator {:error  "Please fill out this field!"
  ;                                :test-f #(-> % empty? not)})
  [validator-id validator]
  (common-state/assoc-state! :form-validator :validators validator-id validator))
