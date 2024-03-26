
(ns form-validator.validator.messages)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn invalid-validator-type-error
  ; @ignore
  ;
  ; @param (keyword) input-id
  ; @param (*) validator
  ;
  ; @return (string)
  [input-id validator]
  (str "Invalid validator has been provided.\n" input-id "\n" validator))

(defn missing-validator-error
  ; @ignore
  ;
  ; @param (keyword) input-id
  ; @param (keyword) validator
  ;
  ; @return (string)
  [input-id validator]
  (str "Registered validator not found:\n" input-id "\n" validator))
