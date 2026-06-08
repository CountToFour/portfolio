const awsServerlessExpress = require('aws-serverless-express');
const app = require('./app');

// Tworzymy serwer AWS Serverless Express
const server = awsServerlessExpress.createServer(app);

// Handler dla AWS Lambda
exports.handler = (event, context) => {
  awsServerlessExpress.proxy(server, event, context);
};
