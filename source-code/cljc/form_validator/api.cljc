
(ns form-validator.api
    (:refer-clojure :exclude [get-validator])
    (:require [form-validator.form.env           :as form.env]
              [form-validator.form.side-effects  :as form.side-effects]
              [form-validator.input.env          :as input.env]
              [form-validator.input.side-effects :as input.side-effects]
              [form-validator.validator.side-effects :as validator.side-effects]
              [form-validator.validator.env :as validator.env]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------
 
; @redirect (form-validator.form.env/*)
(def get-form-inputs form.env/get-form-inputs)

; @redirect (form-validator.form.side-effects/*)
(def autovalidate-form! form.side-effects/autovalidate-form!)
(def validate-form!     form.side-effects/validate-form!)

; @redirect (form-validator.input.env/*)
(def get-input-error             input.env/get-input-error)
(def get-input-value             input.env/get-input-value)
(def get-input-validators        input.env/get-input-validators)
(def validate-input-when-change? input.env/validate-input-when-change?)
(def validate-input-when-leave?  input.env/validate-input-when-leave?)
(def get-input-validation-result input.env/get-input-validation-result)

; @redirect (form-validator.input.side-effects/*)
(def reg-input!          input.side-effects/reg-input!)
(def dereg-input!        input.side-effects/dereg-input!)
(def autovalidate-input! input.side-effects/autovalidate-input!)
(def validate-input!     input.side-effects/validate-input!)
(def input-changed       input.side-effects/input-changed)
(def input-left          input.side-effects/input-left)

; @redirect (form-validator.validator.env/*)
(def get-validator validator.env/get-validator)

; @redirect (form-validator.validator.side-effects/*)
(def reg-validator! validator.side-effects/reg-validator!)
