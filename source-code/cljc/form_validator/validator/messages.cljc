
(ns form-validator.validator.messages)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn invalid-validator-function-error
  ; @ignore
  ;
  ; @param (keyword) input-id
  ; @param (*) validator
  ;
  ; @return (string)
  [input-id validator]
  (str "Provided test function of validator must be a function.\n" input-id "\n" validator))

(defn invalid-validator-type-error
  ; @ignore
  ;
  ; @param (keyword) input-id
  ; @param (*) validator
  ;
  ; @return (string)
  [input-id validator]
  (str "Provided validator must be a map.\n" input-id "\n" validator))

(defn missing-validator-error
  ; @ignore
  ;
  ; @param (keyword) input-id
  ; @param (keyword) validator
  ;
  ; @return (string)
  [input-id validator]
  (str "Registered validator not found:\n" input-id "\n" validator))
