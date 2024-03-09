
(ns form-validator.form.env
    (:require [local-state.api :as local-state]
              [fruits.map.api :as map]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

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
  (if-let [get-value-f (local-state/get-state :form-validator input-id :get-value-f)]
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
  (local-state/get-state :form-validator input-id :validators))

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
                                      (-> error (or :invalid-input-value)))))]
              (if-let [error (vector/first-result validators f0)]
                      {:input-id input-id :input-value internal-value :input-valid? false :error error}
                      {:input-id input-id :input-value internal-value :input-valid? true}))))
