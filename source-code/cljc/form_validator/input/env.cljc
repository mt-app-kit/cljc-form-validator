
(ns form-validator.input.env
    (:require [common-state.api :as common-state]
              [fruits.map.api :as map]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-input-error
  ; @description
  ; Returns the previously stored ':error' value of the input (if any).
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
  (common-state/get-state :form-validator input-id :error))

(defn get-input-value
  ; @description
  ; Applies the 'get-value-f' function of the input and returns the result.
  ;
  ; @param (keyword) input-id
  ;
  ; @usage
  ; (get-input-value :my-input)
  ; =>
  ; "..."
  ;
  ; @return (*)
  [input-id]
  (if-let [get-value-f (common-state/get-state :form-validator input-id :get-value-f)]
          (get-value-f)))

(defn get-input-validators
  ; @description
  ; Returns the validators of the input.
  ;
  ; @param (keyword) input-id
  ;
  ; @usage
  ; (get-input-validators :my-input)
  ; =>
  ; [{:error "Please fill out this field!" :test-f #(-> % empty? not)}]
  ;
  ; @return (maps in vector)
  ; [(map) validator
  ;   {:error (*)
  ;    :test-f (function)}]
  [input-id]
  (common-state/get-state :form-validator input-id :validators))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn validate-input-when-change?
  ; @description
  ; Returns TRUE if the input is registered with the '{:validate-when-change? true}' setting.
  ;
  ; @param (keyword) input-id
  ;
  ; @usage
  ; (validate-input-when-change? :my-input)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [input-id]
  (common-state/get-state :form-validator input-id :validate-when-change?))

(defn validate-input-when-leave?
  ; @description
  ; Returns TRUE if the input is registered with the '{:validate-when-leave? true}' setting.
  ;
  ; @param (keyword) input-id
  ;
  ; @usage
  ; (validate-input-when-leave? :my-input)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [input-id]
  (common-state/get-state :form-validator input-id :validate-when-leave?))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-input-validation-result
  ; @description
  ; - Applies the validators of the input and returns the result of the validation.
  ; - Stops applying further validators in case of one failed.
  ; - The returned result contains the invalid message of the first failed validator.
  ;
  ; @param (keyword) input-id
  ;
  ; @usage
  ; (get-input-validation-result :my-input)
  ; =>
  ; {:input-id :my-input :input-value "..." :input-valid? false :error "Please fill out this field!"}
  ;
  ; @usage
  ; (get-input-validation-result :my-input)
  ; =>
  ; {:input-id :my-input :input-value "..." :input-valid? true}
  ;
  ; @return (map)
  ; {:error (*)
  ;  :input-id (keyword)
  ;  :input-valid? (boolean)
  ;  :input-value (*)}
  [input-id]
  (let [input-value      (get-input-value      input-id)
        input-validators (get-input-validators input-id)]
       (letfn [(f0 [{:keys [test-f error]}]
                   (if test-f (if-not (-> input-value test-f)
                                      (-> error (or "Invalid value")))))]
              (if-let [error (vector/first-result input-validators f0)]
                      {:input-id input-id :input-value input-value :input-valid? false :error error}
                      {:input-id input-id :input-value input-value :input-valid? true}))))
