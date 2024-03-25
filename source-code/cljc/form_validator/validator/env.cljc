
(ns form-validator.validator.env
    (:refer-clojure :exclude [get-validator])
    (:require [common-state.api :as common-state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-validator
  ; @description
  ; Returns a registered form input validator.
  ;
  ; @param (keyword) validator-id
  ;
  ; @usage
  ; (get-validator :my-validator)
  ; =>
  ; {:error "Please fill out this field!" :test-f #(-> % empty? not)}
  ;
  ; @return (map)
  [validator-id]
  (common-state/get-state :form-validator :validators validator-id))
