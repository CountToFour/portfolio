const AWS = require('aws-sdk');
const verifier = require("../middleware/verifier");
const { saveLogs } = require('./saveLogs');

AWS.config.update({ region: 'eu-north-1' });
const s3 = new AWS.S3();

const BUCKET_NAME = 'clouds-project-storage';

const downloadFile = async (req, res) => {
    const { fileName } = req.params;
    const {versionId} = req.query;
    const token = req.headers.authorization || req.headers.Authorization;

    console.log(fileName)
    console.log(versionId)

    if (!token) {
        return res.status(401).json({ error: "Authorization token is missing" });
    }

    try {
      // Pobieranie pliku z S3

        const payload = await verifier.verify(token);
        console.log("Token verified:", payload);


        const params = {
            Bucket: BUCKET_NAME,
            Key: `${payload.username}/${fileName}`,
            VersionId: versionId,
    };


        const data = await s3.getObject(params).promise();

        await saveLogs({
            timestamp: new Date().toISOString(),
            message: `File downloaded ${fileName} for user: ${payload.username}`
        });


        const base64File = data.Body.toString('base64');

      // Ustawienie nagłówków odpowiedzi
      res.setHeader('Content-Type', 'application/json');
      res.setHeader('Content-Disposition', `attachment; filename="${fileName}"`);
  
      // Wysłanie pliku w base64
      res.json({ base64File });// `data.Body` zawiera dane pliku w formie binarnej
    } catch (err) {
        if (err.message === "Invalid signature"){
            console.error("Error:", err.message);
            res.status(401).json({ error: `Error: ${err.message}` });
        } else {
            console.error("Error:", err.message);
            res
                .status(err.name === "TokenExpiredError" ? 401 : 500)
                .json({ error: `Error: ${err.message}` });
        }

    }
  };


  module.exports = { downloadFile }