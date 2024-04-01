
(ns form-validator.form.side-effects
    (:require [form-validator.form.env           :as form.env]
              [form-validator.input.side-effects :as input.side-effects]
              [fruits.vector.api                 :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn validate-form!
  ; @description
  ; Applies the validators of all inputs associated with the given form ID, and ...
  ; ... stores the ':error' values of INVALID inputs in the form validator state.
  ; ... clears the previously stored ':error' values of now VALID inputs from the form validator state.
  ; ... in case of ALL inputs are valid, fires the given ':on-valid-f' function.
  ; ... in case of ANY input is invalid, fires the given ':on-invalid-f' function.
  ;
  ; @param (keyword) form-id
  ; @param (keyword)(opt) validation-props
  ; {:on-invalid-f (function)(opt)
  ;  :on-valid-f (function)(opt)}
  ;
  ; @usage
  ; (def MY-VALIDATOR {:error "Please fill out this field!" :test-f #(-> % empty? not)})
  ; (reg-input!     :my-input {:form-id :my-form :get-value-f #(deref MY-ATOM) :validators [MY-VALIDATOR]})
  ; (validate-form! :my-form  {...})
  ; =>
  ; [{:input-id :my-input :input-value "My value" :input-valid? true}]
  ;
  ; @usage
  ; (reg-validator! :my-validator {:error "Please fill out this field!" :test-f #(-> % empty? not)})
  ; (reg-input!     :my-input     {:form-id :my-form :get-value-f #(deref MY-ATOM) :validators [:my-validator]})
  ; (validate-form! :my-form      {...})
  ; =>
  ; [{:input-id :my-input :input-value "My value" :input-valid? true}]
  ;
  ; @return (maps in vector)
  ; [(map) validation-result
  ;   {:error (*)
  ;    :input-id (keyword)
  ;    :input-valid? (boolean)
  ;    :input-value (*)}]
  ([form-id]
   (validate-form! form-id {}))

  ([form-id {:keys [on-invalid-f on-valid-f]}]
   (let [form-inputs (form.env/get-form-inputs form-id)]
        (letfn [(f0 [input-id] (input.side-effects/validate-input! input-id))]
               (let [validation-results (vector/->items form-inputs f0)]
                    (letfn [(f0 [_] (if on-valid-f   (on-valid-f)))
                            (f1 [_] (if on-invalid-f (on-invalid-f)))]
                           (if (-> validation-results (vector/all-items-match? :input-valid?))
                               (-> validation-results f0)
                               (-> validation-results f1)))
                    (-> validation-results))))))
