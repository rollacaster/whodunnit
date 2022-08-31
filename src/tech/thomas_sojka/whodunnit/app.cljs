(ns tech.thomas-sojka.whodunnit.app
  (:require ["firebase/app" :as firebase]
            ["firebase/auth" :as auth]
            ["firebase/firestore" :as firestore]))

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
