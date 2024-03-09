
(ns form-validator.form.side-effects
    (:require [form-validator.form.env           :as form.env]
              [form-validator.input.side-effects :as input.side-effects]
              [fruits.vector.api                 :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn autovalidate-form!
  ; @description
  ; Sets the ':validate-when-change?' property to TRUE of all inputs associated with the given form ID,
  ; to ensure that the inputs are getting validated when their value changes.
  ;
  ; @param (keyword) form-id
  ;
  ; @usage
  ; (autovalidate-form! :my-form)
  [form-id]
  (let [form-inputs (form.env/get-form-inputs form-id)]
       (doseq [input-id form-inputs]
              (input.side-effects/autovalidate-input! input-id))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn validate-form!
  ; @description
  ; Applies the validators of all inputs associated with the given form ID, and ...
  ; ... stores the ':error' values of INVALID inputs in the form validator state.
  ; ... clears the previously stored ':error' values of now VALID inputs from the form validator state.
  ; ... in case of ALL inputs are valid, fires the given ':on-valid' event.
  ; ... in case of ANY input is invalid, fires the given ':on-invalid' event.
  ;
  ; @param (keyword) form-id
  ; @param (keyword) validation-props
  ; {:on-invalid-f (function)(opt)
  ;  :on-valid-f (function)(opt)}
  ;
  ; @usage
  ; (validate-form! :my-form {...})
  ; =>
  ; [{:input-id :my-input :input-value "..." :input-valid? true}
  ;  ...]
  ;
  ; @return (maps in vector)
  ; [(map) validation-result
  ;   {:error (*)
  ;    :input-id (keyword)
  ;    :input-valid? (boolean)
  ;    :input-value (*)}]
  [form-id {:keys [on-invalid-f on-valid-f] :as validation-props}]
  (let [form-inputs (form.env/get-form-inputs form-id)]
       (letfn [(f0 [input-id] (input.side-effects/validate-input! input-id {}))]
              (let [validation-results (vector/->items form-inputs f0)]
                   (letfn [(f0 [_] (on-valid-f))
                           (f1 [_] (on-invalid-f))]
                          (if (-> validation-results (vector/all-items-match? :input-valid?))
                              (-> validation-results f0)
                              (-> validation-results f1)))
                   (-> validation-results)))))
