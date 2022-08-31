(ns tech.thomas-sojka.whodunnit.app
  (:require ["date-fns" :as date-fns]
            ["firebase/app" :as firebase]
            ["firebase/auth" :as auth]
            ["firebase/firestore" :as firestore]
            [reagent.core :as r]
            [reagent.dom :as dom]
            [tech.thomas-sojka.whodunnit.icons :as icons]))

(def initializeApp (.-initializeApp firebase))

(def firebaseConfig
  #js {:apiKey "AIzaSyCO-kbWA3D__MF4D4J26liRnc0RXwcn1GI",
       :authDomain "whodunnit-8fd8f.firebaseapp.com",
       :projectId "whodunnit-8fd8f",
       :storageBucket "whodunnit-8fd8f.appspot.com",
       :messagingSenderId "831787028872",
       :appId "1:831787028872:web:9d9b901e447f4c9a332a3d"})

(def app (initializeApp firebaseConfig))

(def db (.getFirestore firestore app))

(def auth (auth/getAuth app))

(comment
  (auth/onAuthStateChanged auth (fn [user] (when user (prn (.-uid user)))))
  (auth/signInWithEmailAndPassword auth "thsojka@web.de" "test123")
  (-> (firestore/getDocs (firestore/collection db "test"))
      (.then (fn [snapshot] (.forEach snapshot #(-> % .data js/console.log)))))
  #_(auth/createUserWithEmailAndPassword auth "thsojka@web.de" "test123")
  (auth/signOut auth))

(defn format-date [date]
  ((.-format date-fns) date "dd.MM.yyyy"))

(def user (r/atom {:name "Livi"}))

(def history
  (r/atom
   [{:date (new js/Date 2022 7 30)
     :userId "Kirstin"
     :status :watering}
    {:date (new js/Date 2022 7 29)
     :userId "Livi"
     :status :watering}
    {:date (new js/Date 2022 7 31)
     :userId "Livi"
     :status :rain}]))

(def today (new js/Date))

(defn compute-state [history]
  (reduce
   (fn [state {:keys [userId status]}]
     (case status
       :watering (-> state
                     (assoc :state :watering
                            :userId userId))
       :reject (-> state
                   (assoc :state :dry))
       :rain (-> state
                 (assoc :state :rain
                        :userId userId))))
   {:state :dry
    :date today}
   (->> history
        (filter (fn [{:keys [date]}] (date-fns/isSameDay date today))))))

(defn- current-state [{:keys [state]}]
  [:div.flex.flex-col.items-center.pb-16
   (case state
     :dry [:<>
           [:div.w-36
            [icons/icon {:class "text-red-300"} :close]]
           [:div.text-2xl.text-center.text-red-400 "Blumen haben Durst"]]
     :watering [:<>
                [:div.w-36
                 [icons/icon {:class "text-green-300"} :check]]
                [:div.text-2xl.text-center.text-green-400 "Blumen sind glücklich"]]
     :rain [:<>
            [:div.w-36
             [icons/icon {:class "text-blue-300"} :check]]
            [:div.text-2xl.text-center.text-blue-400 "Es soll regnen"]])])

(defn- watering-button [{:keys [state on-click]}]
  [:button {:class [(when (not= state :dry) "opacity-25")]
            :disabled (not= state :dry)
            :on-click on-click}
   [:div.w-32.text-green-300
    [icons/icon :check]]
   [:div.text-2xl.text-center.text-green-400 "Ich gieße"]])

(defn- rain-button [{:keys [state on-click]}]
  [:button {:class [(when (not= state :dry) "opacity-25")]
            :disabled (not= state :dry)
            :on-click on-click}
   [:div.w-32.text-blue-300
    [icons/icon :rain]]
   [:div.text-2xl.text-center.text-blue-400 "Es soll regnen"]])

(defn main []
  (let [{:keys [state date]} (compute-state @history)]
    [:div
     [:main
      [:header.bg-green-400
       [:div.mx-auto.max-w-5xl.py-4.px-4
        [:h1.text-white.text-3xl.font-bold "Whodunnit?"]]]
      [:section.flex-col.flex.items-center.pt-8.max-w-5xl.mx-auto.px-4
       [:div.text-4xl.pb-16.text-gray-700 (format-date date)]
       [current-state {:state state}]
       [:div.flex.justify-between.w-full.pb-16
        [watering-button {:state state :on-click #(swap! history conj {:status :watering :date today :userId (:name @user)})}]
        [rain-button {:state state :on-click #(swap! history conj {:status :rain :date today :userId (:name @user)})}]]
       [:div.w-full
        [:h2.text-2xl.text-gray-700.pb-3 "Historie"]
        [:ul
         (->> @history
              (sort-by :date >)
              (map
               (fn [{:keys [date userId status]}]
                 ^{:key [date status]}
                 [:li.flex
                  [:span {:class "w-1/5"} userId]
                  [:span {:class "w-2/5"} (format-date date)]
                  [:span {:class "w-2/5"}
                   (case status
                     :watering "Gegoßen"
                     :rain "Regen")]])))]]]]]))

(dom/render [main] (js/document.getElementById "app"))
