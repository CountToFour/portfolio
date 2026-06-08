const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');

const app = express();


app.use(bodyParser.json({ limit: '100mb' }));
app.use(bodyParser.urlencoded({ limit: '100mb', extended: true }));

app.use((req, res, next) => {
    res.setHeader('Access-Control-Allow-Origin', '*'); // Pozwala na wszystkie pochodzenia
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS'); // Dozwolone metody
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization'); // Dozwolone nagłówki
    next();
});

// Konfiguracja AWS SDK


const FOLDER = 'photos/';

// Testowy endpoint
app.get('/files/test', (req, res) => {
  res.status(200).send('Hello World');
});

const routes = require('./router/index')

app.use('/files', routes);


module.exports = app;
