const AWS = require("aws-sdk");
const { v4: uuidv4 } = require("uuid");
const verifier = require("../middleware/verifier");
const path = require("node:path");
const { saveLogs } = require('./saveLogs');
const BUCKET_NAME = "clouds-project-storage"; // Nazwa Twojego bucketu
const s3 = new AWS.S3();
const dynamoDB = new AWS.DynamoDB.DocumentClient();
const sqs = new AWS.SQS(); // Inicjalizacja klienta SQS

const QUEUE_URL = "https://sqs.eu-north-1.amazonaws.com/779846817585/file-processing-queue";

const TABLE_NAME = "FileProcessingTickets";

// Funkcja do zapisywania statusu ticketu w DynamoDB
const saveTicketStatus = async (ticketId, status) => {
    const params = {
        TableName: TABLE_NAME,
        Item: {
            ticketId: ticketId,
            status: status,
            timestamp: Date.now(),
        },
    };
    await dynamoDB.put(params).promise();
};

// Funkcja do uploadu pliku
const uploadFile = async (req, res) => {
    const { fileName } = req.params;
    const { base64File } = req.body;
    const token = req.headers.authorization || req.headers.Authorization;

    if (!token) {
        await saveLogs({
            timestamp: new Date().toISOString(),
            message: `Authorization token is missing`
        });
        return res.status(401).json({ error: "Authorization token is missing" });
    }

    if (!fileName || !base64File) {
        await saveLogs({
            timestamp: new Date().toISOString(),
            message: `No filename or file content provided`
        });
        return res
            .status(400)
            .json({ error: "No file name or file content provided" });
    }

    try {
        // Weryfikacja tokena Cognito
        const payload = await verifier.verify(token);
        console.log("Token verified:", payload);

        const decodedFile = Buffer.from(base64File, "base64");
        console.log("Decoded file length:", decodedFile.length);

        const fileExtension = path.extname(fileName).toLowerCase();
        console.log(fileExtension);

        if (fileExtension === ".zip") {
            await saveLogs({
                timestamp: new Date().toISOString(),
                message: `Generting unique ticket for user: ${payload.username}`
            });
            const ticketId = uuidv4(); // Generujemy unikalny ticket
            await saveTicketStatus(ticketId, "Processing");

            // Wysyłanie wiadomości do SQS
            const messageBody = JSON.stringify({
                ticketId,
                username: payload.username,
                fileName,
                base64File,
            });

            const sqsParams = {
                QueueUrl: QUEUE_URL,
                MessageBody: messageBody,
                DelaySeconds: 30, // Opóźnienie wiadomości
            };

            await sqs.sendMessage(sqsParams).promise();
            await saveLogs({
                timestamp: new Date().toISOString(),
                message: `Message sent to SQS for ticket ${ticketId} for user: ${payload.username}`
            });
            console.log(`Message sent to SQS for ticket ${ticketId}`);
            await saveLogs({
                timestamp: new Date().toISOString(),
                message: `File is being processed for user: ${payload.username}`
            });
            res.status(200).json({ message: "File is being processed", ticketId });
        } else {
            const params = {
                Bucket: BUCKET_NAME,
                Key: `${payload.username}/${fileName}`,
                Body: decodedFile,
            };

            await s3.upload(params).promise();
            await saveLogs({
                timestamp: new Date().toISOString(),
                message: `File uploaded ${fileName} for user: ${payload.username}`
            });
            console.log(`File uploaded successfully for user: ${payload.username}`);

            res.status(200).json({ message: "File uploaded successfully" });
        }
    } catch (err) {
        console.error("Error:", err.message);
        res.status(500).json({ error: `Error: ${err.message}` });
    }
};

module.exports = { uploadFile };