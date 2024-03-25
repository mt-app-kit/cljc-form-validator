
(ns form-validator.input.side-effects
    (:require [common-state.api         :as common-state]
              [form-validator.input.env :as input.env]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn reg-input!
  ; @description
  ; Stores the given properties of the input (in the form validator state).
  ;
  ; @param (keyword) input-id
  ; @param (map) input-props
  ; {:form-id (keyword)(opt)
  ;  :get-value-f (function)
  ;  :validate-when-change? (boolean)(opt)
  ;  :validate-when-leave? (boolean)(opt)
  ;  :validators (keywords and/or maps in vector)(opt)
  ;   [(keyword or map) validator
  ;      {:error (*)(opt)
  ;       :test-f (function)}]
  ;       ...}
  ;
  ; @usage
  ; (reg-input! :my-input {:form-id     :my-form
  ;                        :get-value-f #(deref MY-ATOM)
  ;                        :validators  [{:error "Please fill out this field!" :test-f #(-> % empty? not)}]})
  ;
  ; @usage
  ; (reg-validator! :my-validator {:error "Please fill out this field!" :test-f #(-> % empty? not)})
  ; (reg-input!     :my-input     {:form-id     :my-form
  ;                                :get-value-f #(deref MY-ATOM)
  ;                                :validators  [:my-validator]})
  [input-id input-props]
  (common-state/assoc-state! :form-validator :inputs input-id input-props))

(defn dereg-input!
  ; @description
  ; Removes the properties of the input (from the form validator state).
  ;
  ; @param (keyword) input-id
  ;
  ; @usage
  ; (dereg-input! :my-input)
  [input-id]
  (common-state/dissoc-state! :form-validator :inputs input-id))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn autovalidate-input!
  ; @description
  ; Sets the ':validate-when-change?' property to TRUE of the input, to ensure that it is getting validated when its value changes.
  ;
  ; @param (keyword) input-id
  ;
  ; @usage
  ; (autovalidate-input! :my-input)
  [input-id]
  (common-state/assoc-state! :form-validator :inputs input-id :validate-when-change? true))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn validate-input!
  ; @description
  ; Applies the validators of the input ...
  ; ... and in case of ANY validator failed, it ...
  ;     ... stores the ':error' value of the first failed validator in the form validator state,
  ;     ... fires the given ':on-invalid-f' function (if any),
  ; ... and in case of NO validator failed, it ...
  ;     ... clears the previously stored ':error' value (if any) from the form validator state,
  ;     ... fires the given ':on-valid-f' function (if any),
  ;
  ; @param (keyword) input-id
  ; @param (keyword) validation-props
  ; {:on-invalid-f (function)(opt)
  ;  :on-valid-f (function)(opt)}
  ;
  ; @usage
  ; (validate-input! :my-input {...})
  ; =>
  ; {:input-id :my-input :input-value "..." :input-valid? true}
  ;
  ; @return (map)
  ; {:error (*)
  ;  :input-id (keyword)
  ;  :input-valid? (boolean)
  ;  :input-value (*)}
  [input-id {:keys [on-invalid-f on-valid-f]}]
  (let [validation-result (input.env/get-input-validation-result input-id)]
       (letfn [(f0 [_] (common-state/dissoc-state! :form-validator :inputs input-id :error))
               (f1 [%] (common-state/assoc-state!  :form-validator :inputs input-id :error (:error %)))
               (f2 [%] (if on-valid-f   (on-valid-f   (:input-value %))))
               (f3 [%] (if on-invalid-f (on-invalid-f (:input-value %) (:error %))))]
              (when (-> validation-result :input-valid?)
                    (-> validation-result f0)
                    (-> validation-result f2))
              (when (-> validation-result :input-valid? not)
                    (-> validation-result f1)
                    (-> validation-result f3))
              (-> validation-result))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn input-changed
  ; @description
  ; Validates the input in case it is registered with the '{:validate-when-change? true}' setting.
  ;
  ; @param (keyword) input-id
  ; @param (keyword) validation-props
  ; {:on-invalid-f (function)(opt)
  ;  :on-valid-f (function)(opt)}
  ;
  ; @usage
  ; (input-changed :my-input {...})
  [input-id validation-props]
  (if (input.env/validate-input-when-change? input-id)
      (validate-input!                       input-id validation-props)))

(defn input-left
  ; @description
  ; Validates the input in case it is registered with the '{:validate-when-leave? true}' setting.
  ;
  ; @param (keyword) input-id
  ; @param (keyword) validation-props
  ; {:on-invalid-f (function)(opt)
  ;  :on-valid-f (function)(opt)}
  ;
  ; @usage
  ; (input-left :my-input {...})
  [input-id validation-props]
  (if (input.env/validate-input-when-leave? input-id)
      (validate-input!                      input-id validation-props)))
