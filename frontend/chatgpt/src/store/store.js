import Vue from 'vue'
import Vuex from 'vuex'
Vue.use(Vuex)

const store = new Vuex.Store({
  state: {
    token: '',
  },
  getters: {},
  mutations: {
    setToken(state, token) {
      // console.log('setToken is called with', token);
      state.token = token
     
      localStorage.setItem('token', token);
    }

  },
  actions: {},
})

export default store
