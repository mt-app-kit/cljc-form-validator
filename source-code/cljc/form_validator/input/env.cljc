
(ns form-validator.input.env
    (:require [common-state.api             :as common-state]
              [form-validator.validator.env :as validator.env]
              [fruits.map.api               :as map]
              [fruits.vector.api            :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn input-visited?
  ; @description
  ; Returns TRUE if a specific input is marked as visited.
  ;
  ; @param (keyword) input-id
  ;
  ; @usage
  ; (input-visited? :my-input)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [input-id]
  (common-state/get-state :form-validator :inputs input-id :visited?))

(defn get-input-error
  ; @description
  ; Returns the previously stored ':error' value of a specific input (if any).
  ;
  ; @param (keyword) input-id
  ;
  ; @usage
  ; (get-input-error :my-input)
  ; =>
  ; "Please fill out this field!"
  ;
  ; @return (*)
  [input-id]
  (common-state/get-state :form-validator :inputs input-id :error))

(defn get-input-value
  ; @description
  ; Applies the provided 'get-value-f' function of a specific input and returns the result.
  ;
  ; @param (keyword) input-id
  ;
  ; @usage
  ; (reg-input!      :my-input {:get-value-f #(deref MY-ATOM)})
  ; (get-input-value :my-input)
  ; =>
  ; "My value"
  ;
  ; @return (*)
  [input-id]
  (if-let [get-value-f (common-state/get-state :form-validator :inputs input-id :get-value-f)]
          (get-value-f)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-input-validators
  ; @description
  ; Returns the provided validators of a specific input.
  ;
  ; @param (keyword) input-id
  ;
  ; @usage
  ; (def MY-VALIDATOR {:error "Please fill out this field!" :test-f #(-> % empty? not)})
  ; (reg-input!           :my-input {:validators [MY-VALIDATOR]})
  ; (get-input-validators :my-input)
  ; =>
  ; [{:error "Please fill out this field!" :test-f #(-> % empty? not)}]
  ;
  ; @return (maps in vector)
  ; [(map) validator
  ;   {:error (*)
  ;    :test-f (function)
  ;    :when-changed? (boolean)
  ;    :when-left? (boolean)
  ;    :when-visited? (boolean)}]
  [input-id]
  (letfn [(f0 [%] (validator.env/use-validator input-id %))]
         (if-let [validators (common-state/get-state :form-validator :inputs input-id :validators)]
                 (-> validators (vector/->items f0)))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-input-validation-result
  ; @description
  ; Returns the result of applying the given validators on the value of a specific input returned by the provided 'get-value-f' function.
  ;
  ; @param (keyword) input-id
  ; @param (keywords and/or maps in vector) validators
  ;
  ; @usage
  ; (def MY-VALIDATOR {:error "Please fill out this field!" :test-f #(-> % empty? not)})
  ; (reg-input!                  :my-input {:get-value-f #(deref MY-ATOM)})
  ; (get-input-validation-result :my-input [MY-VALIDATOR])
  ; =>
  ; {:input-id :my-input :input-value "My value" :input-valid? false :error "Please fill out this field!"}
  ;
  ; @usage
  ; (reg-validator!              :my-validator {:error "Please fill out this field!" :test-f #(-> % empty? not)})
  ; (reg-input!                  :my-input     {:get-value-f #(deref MY-ATOM)})
  ; (get-input-validation-result :my-input     [:my-validator])
  ; =>
  ; {:input-id :my-input :input-value "My value" :input-valid? false :error "Please fill out this field!"}
  ;
  ; @return (map)
  ; {:error (*)
  ;  :input-id (keyword)
  ;  :input-valid? (boolean)
  ;  :input-value (*)}
  [input-id validators]
  (let [input-value (get-input-value input-id)]
       (letfn [(f0 [%] (let [{:keys [error test-f]} (validator.env/use-validator input-id %)]
                            (if test-f (if-not (-> input-value test-f)
                                               (-> error (or "Invalid value"))))))]
              (if-let [error (vector/first-result validators f0)]
                      {:input-id input-id :input-value input-value :input-valid? false :error error}
                      {:input-id input-id :input-value input-value :input-valid? true}))))
