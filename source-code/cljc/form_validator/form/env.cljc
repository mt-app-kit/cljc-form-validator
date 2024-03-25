
(ns form-validator.form.env
    (:require [common-state.api :as common-state]
              [fruits.map.api   :as map]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-form-inputs
  ; @description
  ; Returns the IDs of inputs associated with the given form ID.
  ;
  ; @param (keyword) form-id
  ;
  ; @usage
  ; (get-form-inputs :my-form)
  ; =>
  ; [:my-input :another-input]
  ;
  ; @return (keywords in vector)
  [form-id]
  (letfn [(f0 [%] (-> % :form-id (= form-id)))]
         (-> (common-state/get-state :form-validator :inputs)
             (map/filter-values f0)
             (map/keys))))
