
(ns form-validator.validator.side-effects
    (:require [common-state.api :as common-state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn reg-validator!
  ; @description
  ; Registers a reusable form input validator.
  ;
  ; @param (keyword) validator-id
  ; @param (map) validator-props
  ; {:error (*)
  ;  :test-f (function)}
  ;
  ; @usage
  ; (reg-validator! :my-validator {:error  "Please fill out this field!"
  ;                                :test-f #(-> % empty? not)})
  [validator-id validator-props]
  (common-state/assoc-state! :form-validator :validators validator-id validator-props))
