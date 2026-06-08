<script setup>
import { Authenticator } from '@aws-amplify/ui-vue';
import '@aws-amplify/ui-vue/styles.css';
import { ref, onMounted } from 'vue';
import { BRow, BCol, BContainer, BButton, BNavbar, BNavbarBrand , vBColorMode} from 'bootstrap-vue-next'
import FilesComponent from './components/FilesComponent.vue';

import axios from 'axios';

</script>

<template>
  <authenticator>
    <template v-slot="{ user, signOut }">
      <BNavbar  v-b-color-mode="'dark'" toggleable="lg" class="custom-navbar" variant="dark">
        <BNavbarBrand> Welcome, {{ user.username }}! </BNavbarBrand>
        <BButton @click="signOut" variant="danger">
          <font-awesome-icon icon="fa-solid fa-sign-out-alt" class="me-2"/>Sign Out
        </BButton>
      </BNavbar>

      <BContainer class="style-container">
          <FilesComponent :user="user"/>
      </BContainer>

    </template>
  </authenticator>
</template>


<style>
  /* .custom-navbar {
  background-color: #5c6bc0; 
  color: #fff; 
} */
.style-container {
  width: 100%;
 }
body {
  background-color: rgb(144, 200, 219); /* Kolor tła */
  background-size: cover; /* Dopasowanie obrazu */
  background-position: center; /* Wyśrodkowanie */
  margin: 0; /* Usuń margines domyślny */
}
</style>