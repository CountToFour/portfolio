const AWS = require('aws-sdk');
const verifier = require("../middleware/verifier");
const { saveLogs } = require('./saveLogs');

AWS.config.update({ region: 'eu-north-1' });
const s3 = new AWS.S3();

const BUCKET_NAME = 'clouds-project-storage';

const getList = async (req, res) => {
    const token = req.headers.authorization || req.headers.Authorization;

    if (!token) {
        return res.status(401).json({ error: "Authorization token is missing" });
    }

    try {
        // Weryfikacja tokena

        const payload = await verifier.verify(token);
        console.log("Token verified:", payload);


        // Parametry listowania plików
        const params = {
            Bucket: BUCKET_NAME,
            Prefix: `${payload.username}/`,
        };

        // Pobieranie wersji obiektów z S3
        const data = await s3.listObjectVersions(params).promise();
        console.log('S3 Object Versions:', data.Versions);

        // Przetwarzanie plików z wersjami
        const files = data.Versions
            .filter((obj) => obj.Key !== `${payload.username}/`) // Pomijamy same foldery
            .map((obj) => ({
                filename: obj.Key.split('/').pop(), // Nazwa pliku
                versionId: obj.VersionId, // ID wersji
                lastModified: obj.LastModified, // Data modyfikacji
                size: obj.Size, // Rozmiar pliku
                isLatest: obj.IsLatest // Czy to najnowsza wersja
            }));

        console.log('Przetworzone pliki:', files);

        await saveLogs({
            timestamp: new Date().toISOString(),
            message: `Get file list for user: ${payload.username}`
        });

        // Zwracanie danych do frontendu
        res.status(200).json({ files });
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

module.exports = { getList };
