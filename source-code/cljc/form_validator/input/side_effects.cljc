
(ns form-validator.input.side-effects
    (:require [common-state.api :as common-state]
              [form-validator.input.env :as input.env]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn reg-input!
  ; @description
  ; Stores the given properties of the input (in the form validator state).
  ;
  ; @param (keyword) input-id
  ; @param (map) input-props
  ; {:form-id (keyword)(opt)
  ;   Allows inputs to be validated in groups based on their form ID.
  ;  :get-value-f (function)
  ;   Must return the input value (provided to validator functions).
  ;  :validators (keywords and/or maps in vector)(opt)
  ;   [(keyword or map) validator
  ;      {:error (*)(opt)
  ;       :test-f (function)
  ;       :when-changed? (boolean)(opt)
  ;       :when-left? (boolean)(opt)
  ;       :when-visited? (boolean)(opt)}]}
  ;
  ; @usage
  ; (def MY-VALIDATOR {:error "Please fill out this field!" :test-f #(-> % empty? not)})
  ; (reg-input! :my-input {:form-id     :my-form
  ;                        :get-value-f #(deref MY-ATOM)
  ;                        :validators  [MY-VALIDATOR]})
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

(defn mark-input-as-visited!
  ; @description
  ; Marks a specific input as visited.
  ;
  ; @param (keyword) input-id
  ;
  ; @usage
  ; (mark-input-as-visited! :my-input)
  [input-id]
  (common-state/assoc-state! :form-validator :inputs input-id :visited? true))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn apply-validators-on-input!
  ; @description
  ; Applies the given validators on a specific input ...
  ; ... and in case of ANY validator failed, it ...
  ;     ... stores the ':error' value of the first failed validator in the form validator state,
  ;     ... fires the given ':on-invalid-f' function (if any),
  ; ... and in case of NO validator failed, it ...
  ;     ... clears the previously stored ':error' value (if any) from the form validator state,
  ;     ... fires the given ':on-valid-f' function (if any).
  ;
  ; @param (keyword) input-id
  ; @param (keywords and/or maps in vector) validators
  ; [(map) validator
  ;   {:error (*)
  ;    :test-f (function)}]
  ; @param (map)(opt) validation-props
  ; {:on-invalid-f (function)(opt)
  ;  :on-valid-f (function)(opt)}
  ;
  ; @usage
  ; (def MY-VALIDATOR {:error "Please fill out this field!" :test-f #(-> % empty? not)})
  ; (reg-input!                 :my-input {:get-value-f #(deref MY-ATOM)})
  ; (apply-validators-on-input! :my-input [MY-VALIDATOR])
  ; =>
  ; {:input-id :my-input :input-value "My value" :input-valid? true}
  ;
  ; @usage
  ; (reg-validator!             :my-validator {:error "Please fill out this field!" :test-f #(-> % empty? not)})
  ; (reg-input!                 :my-input     {:get-value-f #(deref MY-ATOM)})
  ; (apply-validators-on-input! :my-input     [:my-validator])
  ; =>
  ; {:input-id :my-input :input-value "My value" :input-valid? true}
  ;
  ; @return (map)
  ; {:error (*)
  ;  :input-id (keyword)
  ;  :input-valid? (boolean)
  ;  :input-value (*)}
  ([input-id validators]
   (apply-validators-on-input! input-id validators {}))

  ([input-id validators {:keys [on-invalid-f on-valid-f]}]
   (let [validation-result (input.env/get-input-validation-result input-id validators)]
        (common-state/assoc-state! :form-validators :inputs input-id :already-validated? true)
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
               (-> validation-result)))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn validate-input!
  ; @description
  ; Applies all provided validators of a specific input ...
  ; ... and in case of ANY validator failed, it ...
  ;     ... stores the ':error' value of the first failed validator in the form validator state,
  ;     ... fires the given ':on-invalid-f' function (if any),
  ; ... and in case of NO validator failed, it ...
  ;     ... clears the previously stored ':error' value (if any) from the form validator state,
  ;     ... fires the given ':on-valid-f' function (if any).
  ;
  ; @param (keyword) input-id
  ; @param (keyword)(opt) validation-props
  ; {:on-invalid-f (function)(opt)
  ;  :on-valid-f (function)(opt)}
  ;
  ; @usage
  ; (def MY-VALIDATOR {:error "Please fill out this field!" :test-f #(-> % empty? not)})
  ; (reg-input!      :my-input {:get-value-f #(deref MY-ATOM) :validators [MY-VALIDATOR]})
  ; (validate-input! :my-input {...})
  ; =>
  ; {:input-id :my-input :input-value "My value" :input-valid? true}
  ;
  ; @usage
  ; (reg-validator!  :my-validator {:error "Please fill out this field!" :test-f #(-> % empty? not)})
  ; (reg-input!      :my-input     {:get-value-f #(deref MY-ATOM) :validators [:my-validator]})
  ; (validate-input! :my-input     {...})
  ; =>
  ; {:input-id :my-input :input-value "My value" :input-valid? true}
  ;
  ; @return (map)
  ; {:error (*)
  ;  :input-id (keyword)
  ;  :input-valid? (boolean)
  ;  :input-value (*)}
  ([input-id]
   (validate-input! input-id {}))

  ([input-id validation-props]
   (let [input-validators (input.env/get-input-validators input-id)]
        (apply-validators-on-input! input-id input-validators validation-props))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn input-changed
  ; @description
  ; - Applies validators of a specific input provided with '{:when-changed? true}' setting.
  ; - Applies validators of a specific input provided with '{:when-visited? true}' setting if the input has been left before.
  ;
  ; @param (keyword) input-id
  ; @param (keyword)(opt) validation-props
  ; {:on-invalid-f (function)(opt)
  ;  :on-valid-f (function)(opt)}
  ;
  ; @usage
  ; (def MY-VALIDATOR {:when-changed? true :error "Please fill out this field!" :test-f #(-> % empty? not)})
  ; (reg-input!    :my-input {:get-value-f #(deref MY-ATOM) :validators [MY-VALIDATOR]})
  ; (input-changed :my-input {...})
  ; =>
  ; {:input-id :my-input :input-value "MY VALUE" :input-valid? true}
  ;
  ; @usage
  ; (reg-validator! :my-validator {:when-changed? true :error "Please fill out this field!" :test-f #(-> % empty? not)})
  ; (reg-input!     :my-input     {:get-value-f #(deref MY-ATOM) :validators [:my-validator]})
  ; (input-changed  :my-input     {...})
  ; =>
  ; {:input-id :my-input :input-value "MY VALUE" :input-valid? true}
  ;
  ; @return (map)
  ; {:error (*)
  ;  :input-id (keyword)
  ;  :input-valid? (boolean)
  ;  :input-value (*)}
  ([input-id]
   (input-changed input-id {}))

  ([input-id validation-props]
   (let [input-validators (input.env/get-input-validators input-id)]
        (when     (input.env/input-visited?   input-id)
                  (apply-validators-on-input! input-id (-> input-validators (vector/keep-items-by :when-changed?)) validation-props)
                  (apply-validators-on-input! input-id (-> input-validators (vector/keep-items-by :when-visited?)) validation-props))
        (when-not (input.env/input-visited?   input-id)
                  (apply-validators-on-input! input-id (-> input-validators (vector/keep-items-by :when-changed?)) validation-props)))))

(defn input-left
  ; @description
  ; - Applies validators of a specific input provided with '{:when-left? true}' setting.
  ; - Applies validators of a specific input provided with '{:when-visited? true}' setting.
  ; - Marks the input as visited.
  ;
  ; @param (keyword) input-id
  ; @param (keyword)(opt) validation-props
  ; {:on-invalid-f (function)(opt)
  ;  :on-valid-f (function)(opt)}
  ;
  ; @usage
  ; (def MY-VALIDATOR {:when-left? true :error "Please fill out this field!" :test-f #(-> % empty? not)})
  ; (reg-input!    :my-input {:get-value-f #(deref MY-ATOM) :validators [MY-VALIDATOR]})
  ; (input-changed :my-input {...})
  ; =>
  ; {:input-id :my-input :input-value "MY VALUE" :input-valid? true}
  ;
  ; @usage
  ; (reg-validator! :my-validator {:when-left? true :error "Please fill out this field!" :test-f #(-> % empty? not)})
  ; (reg-input!     :my-input     {:get-value-f #(deref MY-ATOM) :validators [:my-validator]})
  ; (input-changed  :my-input     {...})
  ; =>
  ; {:input-id :my-input :input-value "MY VALUE" :input-valid? true}
  ([input-id]
   (input-left input-id {}))

  ([input-id validation-props]
   (mark-input-as-visited! input-id)
   (let [input-validators (input.env/get-input-validators input-id)]
        (apply-validators-on-input! input-id (-> input-validators (vector/keep-items-by :when-left?))    validation-props)
        (apply-validators-on-input! input-id (-> input-validators (vector/keep-items-by :when-visited?)) validation-props))))
