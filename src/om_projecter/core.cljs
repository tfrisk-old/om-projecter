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
            (apply dom/ul nil
              (map #(dom/li nil (get-user-name (:user %))) (:members project))))
          (dom/li nil "tasks"
            (apply dom/ul nil
              (map #(dom/li nil (:name %)) (:tasks project))))
          )))))

(defn project-view [project owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
        (dom/li nil (str (:name project))
        (om/build project-details project))))))

(defn projects-view [app owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
        (dom/h2 nil "Projects list")
        (apply dom/ul nil
          (om/build-all project-view (:projects app)))))))

(om/root projects-view app-state
  {:target (. js/document (getElementById "app"))})
