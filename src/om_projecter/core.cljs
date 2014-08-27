(ns om-projecter.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def app-state (atom {
    :organization "My own organization"
    :change_date "today"
    :projects [
        {
            :id 1
            :name "project management application"
            :start_date "26.8.2014"
            :end_date "31.12.2014"
            :priority 3
            :members [
                {:user 123 :role "a"}
                {:user 234 :role "b"}
            ]
            :tasks [
                {:id 1 :done false :effort [] :start_week 1 :end_week 1
                :name "First draft of data" :users [123]}
                {:id 2 :done false :effort [] :start_week 1 :end_week 1
                :name "First draft of ui" :users [123 345]}
            ]
        }
        {:id 2 :name "stub"}
    ]
    :users [
        {:id 123 :name "Jack"}
        {:id 234 :name "Jill"}
        {:id 345 :name "Jürgen"}
    ]
    :roles [
        {:id "a" :name "Role A"}
        {:id "b" :name "Role B"}
        {:id "c" :name "Role C"}
    ]
}))

;============= editable fields ======

(defn display [show]
  (if show
    #js {}
    #js {:display "none"}))

(defn handle-change [e data edit-key owner]
  (om/transact! data edit-key (fn [_] (.. e -target -value))))

(defn on-edit [id title])

(defn end-edit [text owner cb]
  (om/set-state! owner :editing false)
  (when cb
    (cb text)))

(defn editable [data owner {:keys [edit-key on-edit] :as opts}]
  (reify
    om/IInitState
    (init-state [_]
      {:editing false})
    om/IRenderState
    (render-state [_ {:keys [editing]}]
      (let [text (get data edit-key)]
        (dom/li nil
          (dom/span #js {:style (display (not editing))} text)
          (dom/input
            #js {:style (display editing)
                 :value text
                 :onChange #(handle-change % data edit-key owner)
                 :onKeyPress #(when (== (.-keyCode %) 13)
                                (end-edit text owner on-edit))
                 :onBlur (fn [e]
                           (when (om/get-state owner :editing)
                             (end-edit text owner on-edit)))})
          (dom/button
            #js {:style (display (not editing))
                 :onClick #(om/set-state! owner :editing true)}
            "Edit"))))))

;============= show app data ======

(defn get-user-name [userid]
  (:name (first
    (filter #(= userid (:id %)) (:users @app-state)))))

(defn get-role-name [roleid]
  (:name (first
    (filter #(= roleid (:id %)) (:roles @app-state)))))

(defn project-member-details [member owner]
  (reify
    om/IRender
    (render [_]
      (dom/label nil (str "testi")))))

(defn project-details [project owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
        (dom/ul nil
          (dom/li nil (str "start date: " (:start_date project)))
          (dom/li nil (str "end date: " (:end_date project)))
          (dom/li nil (str "priority: " (:priority project)))
          (dom/li nil "members"
            (dom/table nil
              (apply dom/tr nil
                (dom/th nil "user")
                (dom/th nil "role")
              (map (fn [member]
                (dom/tr nil
                (dom/td nil (get-user-name (:user member)))
                (dom/td nil (get-role-name (:role member)))))
                (:members project)))))
          (dom/li nil "tasks"
            (dom/table nil
              (apply dom/tr nil
                (dom/th nil "name")
                (dom/th nil "start week")
                (dom/th nil "end week")
                (dom/th nil "users")
                (dom/th nil "done?")
              (map (fn [task]
                (dom/tr nil
                (dom/td nil (:name task))
                (dom/td nil (:start_week task))
                (dom/td nil (:end_week task))
                (dom/td nil (apply dom/ul nil
                  (map (fn [uid] (dom/li nil (get-user-name uid)))
                    (:users task))))
                (dom/td nil (str (:done task)))
                )) (:tasks project)))))
          )))))

(defn project-view [project owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
        (dom/li nil (str (:name project))
        (om/build project-details project))))))

(defn list-view [entry owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
        ;(dom/li nil (str (:name entry)))
        (om/build editable entry
          {:opts {:edit-key :name}})
        ))))

(defn projects-view [app owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
        (dom/h2 nil "Projects list")
        (apply dom/ul nil
          (om/build-all project-view (:projects app)))
        (dom/h2 nil "Users")
        (apply dom/ul nil
          (om/build-all list-view (:users app)))
        (dom/h2 nil "Roles")
        (apply dom/ul nil
          (om/build-all list-view (:roles app)))
        ))))

(om/root projects-view app-state
  {:target (. js/document (getElementById "app"))})
