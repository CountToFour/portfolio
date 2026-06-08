<script setup>
import { Authenticator } from '@aws-amplify/ui-vue';
import '@aws-amplify/ui-vue/styles.css';
import { ref, onMounted, computed } from 'vue';
import { BContainer, BTable, BButton, BFormFile, BCol, BFormInput, BInputGroupText, BInputGroup, BFormGroup, BRow, BFormSelect, BCard, BProgress} from 'bootstrap-vue-next'
import { useToast } from 'vue-toastification';

import axios from 'axios';

const file = ref(null)
const filter = ref('')
const selectedFilter = ref('')
const fileList = ref([]);
const tickets = ref([]);
const API_URL = `https://cjaomdnus8.execute-api.eu-north-1.amazonaws.com/dev/files/`;
const toast = useToast();
const showFiles = ref(false)
const showProcessing = ref(false);
const loading = ref(false);
const uploadProgress = ref(0); // Postęp w procentach


const MIN_PART_SIZE = 5242880;

defineProps({
  user: {
    type: Object,
    required: true,
  },
});

const fields = [
  { key: "filename", label: "File Name",  class: 'text-center' },
  { key: "lastModified", label: "Last Modified",  class: 'text-center' },
  { key: "size", label: "Size",  class: 'text-center' },
  { key: "actions", label: "Download",  class: 'text-center' },
  { key: "actions2", label: "See versions", class: "text-center"}
]

const fields2 = [
  { key: "filename", label: "File Name",  class: 'text-center' },
  { key: "ticketId", label: "Ticked ID",  class: 'text-center' },
  { key: "status", label: "Status",  class: 'text-center' },
]

const filters = [
    {id: 'newest', text: "Newest files"},
    {id: 'oldest', text: "Oldest files"},
    {id: 'a-z', text: "Name A-Z"},
    {id: 'z-a', text: "Name Z-A"}
]

const getAccessToken = () => {
  // Iteruj przez wszystkie klucze w localStorage
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i); // Pobierz nazwę klucza

    // Sprawdź, czy klucz pasuje do wzorca
    if (key.startsWith("CognitoIdentityServiceProvider") && key.endsWith("accessToken")) {
      // Jeśli znajdziesz klucz, zwróć wartość
      return localStorage.getItem(key);
    }
  }

  // Jeśli nie znajdziesz tokena, zwróć null
  return null;
};

function getTicketsByTicketId() {

  const tickets = [];
  const TicketId = 'TicketId'

  // Iterujemy przez wszystkie elementy w localStorage
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i); // Pobieramy nazwę klucza
    if (key && key.includes(TicketId)) { // Sprawdzamy, czy nazwa klucza zawiera TicketId
      const item = localStorage.getItem(key); // Pobieramy wartość z localStorage
      //console.log(item);
      try {
        const temp = [];
        // Parsujemy dane
        //const parsedItem = JSON.parse(item);

        // Wyciąganie nazwy pliku z klucza (po "_" i bez liczby na końcu)
        const fileNameWithExtension = key.split('TicketId')[1]; // Zakładamy, że nazwa pliku jest po "_"
        const fileName = fileNameWithExtension.replace(/_?\d+$/, ''); // Usuwamy ewentualną liczbę na końcu
        // console.log(fileName);
        // console.log(fileNameWithExtension);

        // Dodajemy nazwę pliku do obiektu
        //parsedItem.fileName = fileName;

        //tickets.push(item); // Dodajemy do tablicy tickets
        temp.push(item);
        temp.push(fileName);
        tickets.push(temp);
      } catch (e) {
        console.error('Błąd parsowania elementu:', key);
      }
    }

  }

  // console.log(tickets);
  return tickets;
}


const getProcessingFile = async () => {
  loading.value = true;
  if(showFiles.value){
    showFiles.value = false;
  }
  if (showProcessing.value === false) {
    tickets.value = getTicketsByTicketId();
    for (let ticket of tickets.value) {
      try {
        // Wysyłanie pliku jako surowych danych (Blob/File)
        console.log(ticket);
        const response = await axios.get(`${API_URL}ticketStatus/${ticket[0]}`);
        //console.log('Status read successfully:', response.data);
        ticket[2] = response.data.status;
        //toast.success('File uploaded successfully!')
      } catch (err) {
        console.error('Error uploading file:', err);
        if (err.response) {
          console.error('Response error:', err.response.data);
        }
      }
    }
    console.log(tickets.value);
  }
  loading.value = false;
  showProcessing.value = !showProcessing.value;
}

// Funkcja obsługująca zmianę pliku
const handleFileChange = (event) => {
  file.value = event.target.files[0]; // Przypisujemy wybrany plik do file.value
};

function saveItemWithUniqueName(key, value) {
  let uniqueKey = key;
  let counter = 1;

  // Sprawdzanie, czy klucz już istnieje
  while (localStorage.getItem(uniqueKey) !== null) {
    uniqueKey = `${key}_${counter}`;
    counter++;
  }

  // Zapisanie wartości pod unikalnym kluczem
  localStorage.setItem(uniqueKey, value);
  console.log(`Dane zapisane pod kluczem: ${uniqueKey}`);
}


const uploadFile = async (username) => {
    if (file.value.size < 5242880) {
      uploadFileSmaller(username)
      console.log("Rozmiar: " + file.value.size)
    } else {
      uploadFileBigger(username)
      console.log("Rozmiar: " + file.value.size)
    }
}

// Funkcja przesyłająca plik
const uploadFileSmaller = async (username) => {
  if (!file.value) {
    console.error('No file selected');
    toast.error('No file selected')
    return;
  }

  console.log(username);

  const fileName = file.value.name; // Pobieramy nazwę pliku // Dynamiczna ścieżka z nazwą pliku

  // Tworzymy instancję FileReader
  const fileReader = new FileReader();

  const token = getAccessToken();
  if (!token) {
    console.error("No token found in localStorage");
    return;
  }
  console.log(token);


  // Funkcja wykonująca się po zakończeniu odczytu pliku
  fileReader.onload = async function(event) {
    // Zawartość pliku w formacie Base64 (usuwa prefiks data URL)
    const base64File = event.target.result.split(',')[1];

    console.log("file", file.value)
    try {
      // Wysyłanie pliku jako surowych danych (Blob/File)
      const response = await axios.put(`${API_URL}${fileName}`, {
        base64File: base64File,// Zawartość pliku w Base64
      }, {
        headers: {
          'Content-Type': 'application/json', // Typ MIME pliku`
          Authorization: `${token}`,
        },
      });
      if (response.data.ticketId !== undefined) {
        saveItemWithUniqueName(`TicketId${fileName}`, response.data.ticketId);
      }
      console.log('File uploaded successfully:', response.data);
      toast.success('File uploaded successfully!')
    } catch (err) {
      console.error('Error uploading file:', err);
      if (err.response) {
        console.error('Response error:', err.response.data);
      }
    }
  };
  fileReader.readAsDataURL(file.value);
};


// Funkcja przesyłająca plik
const uploadFileBigger = async (username) => {
  if (!file.value) {
    console.error('No file selected');
    toast.error('No file selected');
    return;
  }

  const fileName = file.value.name;
  const token = getAccessToken();
  if (!token) {
    console.error("No token found in localStorage");
    return;
  }

  try {
    // 1. Wysyłamy zapytanie o inicjowanie multipart upload
    const response = await axios.put(`${API_URL}multipart/test`, {
      fileName: fileName,
      fileSize: file.value.size
    }, {
      headers: {
        Authorization: `${token}`,
      },
    });

    const { uploadId, parts } = response.data;

    // 2. Dzielimy plik na części i przesyłamy każdą część
    await uploadFileParts(uploadId, parts, file.value);
    toast.success('File uploaded successfully!');
  } catch (err) {
    console.error('Error uploading file:', err);
    if (err.response) {
      console.error('Response error:', err.response.data);
    }
  }
};

const uploadFileParts = async (uploadId, parts, file) => {
  let partSize = Math.ceil(file.size / parts.length);

  if (partSize < MIN_PART_SIZE) {
    partSize = MIN_PART_SIZE;
  }

  const promises = [];
  const progress = Array(parts.length).fill(0);

  const updateProgress = () => {
    const totalProgress = (progress.reduce((a, b) => a + b, 0) / parts.length) * 100;
    uploadProgress.value = totalProgress.toFixed(2); // Aktualizacja zmiennej
    console.log(`Upload progress: ${uploadProgress.value}%`);
  };

  for (let i = 0; i < parts.length; i++) {
    const start = i * partSize;
    const end = Math.min(start + partSize, file.size);
    const part = file.slice(start, end);

    const partResponse = axios.put(parts[i].url, part, {
      headers: {
        "Content-Type": file.type,
      },
      onUploadProgress: (event) => {
        progress[i] = event.loaded / event.total;
        updateProgress();
      },
    }).then((res) => {
      parts[i] = { ETag: res.headers.etag, PartNumber: i + 1 };
    });

    promises.push(partResponse);
  }

  await Promise.all(promises);
  finishMultipartUpload(uploadId, file.name, parts);
};


const finishMultipartUpload = async (uploadId, fileName, parts) => {
  const token = getAccessToken();
  if (!token) {
    console.error("No token found in localStorage");
    return;
  }

  try {
    const response = await axios.put(`${API_URL}complete/test`, {
      uploadId,
      fileName,
      parts, // Lista części z numerami i ETag (w tym przypadku część URL i ETag)
    }, {
      headers: {
        Authorization: `${token}`,
      },
    });

    console.log('File upload completed successfully:', response.data);
    uploadProgress.value=0
  } catch (err) {
    console.error('Error completing upload:', err);
  }
};

const getFiles = async (username) => {
  showFiles.value = !showFiles.value;
  if(showProcessing.value){
    showProcessing.value = false;
  }
  if (fileList.value.length === 0) {
    loading.value = true;
  }
  console.log(showFiles.value)
  const token = getAccessToken();
  if (!token) {
    console.error("No token found in localStorage");
    return;
  }

  try {
    const response = await axios.get(`${API_URL}list`, {
      headers: {
        'Content-Type': 'application/json', // Typ MIME pliku`
        Authorization: `${token}`,
      },
    });
    loading.value = false;
    console.log("Przesłane dane!!!!")
    console.log(response);

    fileList.value = response.data.files; // Zapisanie listy plików
    console.log('Files:', fileList.value);
  } catch (error) {
    console.error('Error fetching file list:', error);
  }
};

const downloadFile = async (fileName, id) => {
  const token = getAccessToken();
  if (!token) {
    console.error("No token found in localStorage");
    return;
  }

  console.log("Nazwa pliku " + fileName)
  console.log("Jego wersja " + id)

  try {
    const response = await axios.get(`${API_URL}download_file/${fileName}?versionId=${encodeURIComponent(id)}`, {
      headers: {
      'Content-Type': 'application/json', // Typ MIME pliku`
          Authorization: `${token}`,
    },
    });
    // Konwersja Base64 na binarny ArrayBuffer
    const binaryString = atob(response.data.base64File); // Dekodowanie Base64 na string
    const binaryArray = new Uint8Array(binaryString.length);

    // Przekształcenie stringa na array of bytes
    for (let i = 0; i < binaryString.length; i++) {
      binaryArray[i] = binaryString.charCodeAt(i);
    }

    // Tworzenie Blob z danych binarnych
    const blob = new Blob([binaryArray], { type: 'application/octet-stream' });

    // Tworzenie linku do pobrania pliku
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);


    console.log('File downloaded successfully:', fileName);
  } catch (error) {
    console.error('Error downloading file:', error);
  }
};

const sortedFiles = computed(() => {

    let latestFiles = fileList.value.filter(file => file.isLatest);

    if (selectedFilter.value === 'newest') {
        latestFiles = latestFiles.sort((a, b) => new Date(b.lastModified) - new Date(a.lastModified));
    } else if (selectedFilter.value === 'oldest') {
        latestFiles = latestFiles.sort((a, b) => new Date(a.lastModified) - new Date(b.lastModified));
    } else if (selectedFilter.value === "a-z") {
        latestFiles = latestFiles.sort((a, b) => a.filename.localeCompare(b.filename));
    } else if (selectedFilter.value === "z-a") {
        latestFiles = latestFiles.sort((a, b) => b.filename.localeCompare(a.filename));
    } 

    return latestFiles;
});

const fileVersions = (filename) => {
  return fileList.value.filter(file => file.filename === filename && !file.isLatest);
};

const calculateSize = (size) => {
    let temp = 0
    if (size < 1024) {
      return `${size}B`
    } else if (size > 1024 && size < 1048576) {
      temp = Math.round((size / 1000) * 100) / 100
      return `${temp} KB`
    } else {
      temp = Math.round((size / 1000000) * 100) / 100
      return `${temp} MB`
    }
}

</script>

<template>
    <BContainer>
      <div class="d-flex flex-column justify-content-center align-items-center">
        <h1>Prześlij plik przez API</h1>
        <div class="d-flex flex-row align-items-center mt-3">
          <BFormFile v-model="file" @change="handleFileChange" style="width: 400px;" size="lg"/>
          <BButton @click="uploadFile(user.username)" variant="primary" class="ml-3" size="lg" style="margin-left: 12px;">
            <font-awesome-icon icon="fa-solid fa-arrow-up-from-bracket" /> Prześlij plik
          </BButton>
        </div>
        <div v-if="uploadProgress > 0" class="mt-3" style="width: 400px;">
          <BProgress :value="uploadProgress" :max="100" show-progress :precision="2"></BProgress>
        </div>
        <div class="d-flex flex-row align-items-center mt-3">
          <BButton @click="getFiles(user.username)" variant="success" class="mt-4" size="lg">
            <font-awesome-icon icon="fa-solid fa-list" /> {{ showFiles ? "Ukryj pliki" : "Wyświetl pliki"}}
          </BButton>
          <BButton @click="getProcessingFile()" variant="success" class="mt-4" size="lg" style="margin-left: 12px;">
            <font-awesome-icon icon="fa-solid fa-list" /> {{ showProcessing ? "Ukryj przetwarzane pliki" : "Wyświetl przetwarzane pliki"}}
          </BButton>
        </div>
      </div>
      <BRow v-if="showFiles" class="mt-3">
        <BCol lg="4" class="my-1">
                <BFormGroup>
                    <BInputGroup>
                        <BFormInput v-model="filter" type="search" placeholder="Type to Search"/>
                        <BInputGroupText>
                            <BButton :disabled="!filter" @click="filter = ''">Clear</BButton>
                        </BInputGroupText>
                    </BInputGroup>
                </BFormGroup>
            </BCol>
            <BCol lg="4" class="my-1">
                <BFormGroup>
                    <BFormSelect v-model="selectedFilter" :options="[{ value: '', text: 'Filtr By...' }, ...filters.map(filtr => ({ value: filtr.id, text: filtr.text }))]" />
                </BFormGroup>
            </BCol>
      </BRow>
      <BTable v-if="showFiles" :items="sortedFiles" :fields="fields" :filter="filter" striped hover class="mt-3">
        <template #cell(filename)="data">
          {{ data.item.filename }}
        </template>
        <template #cell(lastModified)="data">
            {{ new Date(data.item.lastModified).toLocaleString('pl-PL') }}
        </template>
        <template #cell(size)="data">
          {{ calculateSize(data.item.size) }}
        </template>
        <template #cell(actions)="data">
            <BButton variant="primary" @click="downloadFile(data.item.filename, data.item.versionId)">
                <font-awesome-icon icon="fa-solid fa-download" />
            </BButton>
        </template>
        <template #cell(actions2)="data">
            <BButton variant="primary"  @click="data.toggleDetails">
                {{ data.detailsShowing ? 'Hide' : 'Show' }} versions
            </BButton>
        </template>
        <template #row-details="data">
            <BCard>
                <p class="font-weight-bold"> Versions for file: <strong>{{ data.item.filename }}</strong></p>
                <BTable :items="fileVersions(data.item.filename)" :fields="[
                    { key: 'lastModified', label: 'Last Modified', class: 'text-center'},
                    { key: 'size', label: 'Size', class: 'text-center'},
                    { key: 'actions', label: 'Download', class: 'text-center'}
                    ]" striped bordered hover small>
                    <template #cell(lastModified)="version">
                        {{ new Date(version.item.lastModified).toLocaleString('pl-PL') }}
                    </template>
                    <template #cell(size)="version">
                        {{ calculateSize(version.item.size) }}
                    </template>
                    <template #cell(actions)="version">
                        <BButton variant="primary" @click="downloadFile(version.item.filename, version.item.versionId)">
                            <font-awesome-icon icon="fa-solid fa-download" />
                        </BButton>
                    </template>
                </BTable>
            </BCard>
        </template>
      </BTable>
      <BTable v-if="showProcessing" :items="tickets" :fields="fields2" striped hover class="mt-3">
        <template #cell(filename)="data">
          {{ data.item[1] }}
        </template>
        <template #cell(ticketId)="data">
          {{ data.item[0] }}
        </template>
        <template #cell(status)="data">
          {{ data.item[2] }}
        </template>
      </BTable>
      <BRow v-if="loading && (showFiles || !showProcessing)" style="margin-top: 24px;">
        <div class="d-flex justify-content-center align-items-center">
          <div class="spinner-border" role="status" aria-hidden="true" style="margin-right: 8px;"></div>
          <strong>Loading...</strong>
        </div>
      </BRow>
    </BContainer>
  </template>
  
  <style scoped>
        .color {
            color: blueviolet;
        }

        .custom-table {
  background-color: #343a40; /* Dark background */
  color: #f8f9fa; /* Light text color */
}

.custom-table th {
  background-color: #007bff; /* Blue header */
}

.custom-table tbody tr:hover {
  background-color: #495057; /* Darker background on hover */
}

  </style>
  