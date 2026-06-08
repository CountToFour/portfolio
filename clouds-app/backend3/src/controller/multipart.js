const AWS = require("aws-sdk");
const {saveLogs} = require("./saveLogs");
const verifier = require("../middleware/verifier");
const s3 = new AWS.S3();
const BUCKET_NAME = "clouds-project-storage";

const multipart = async (req, res) => {
    const { fileName, fileSize } = req.body;
    const token = req.headers.authorization || req.headers.Authorization;

    if (!token) {
        await saveLogs({
            timestamp: new Date().toISOString(),
            message: `Authorization token is missing`
        });
        return res.status(401).json({ error: "Authorization token is missing" });
    }

    if (!fileName) {
        await saveLogs({
            timestamp: new Date().toISOString(),
            message: `No filename or file content provided`
        });
        return res
            .status(400)
            .json({ error: "No file name or file content provided" });
    }

    try {
        const payload = await verifier.verify(token);
        console.log("Token verified:", payload);
        // Inicjowanie multipart upload
        const params = {
            Bucket: BUCKET_NAME,
            Key: `${payload.username}/${fileName}`,
            ContentType: req.body.fileType,
        };

        const multipartUpload = await s3.createMultipartUpload(params).promise();
        const uploadId = multipartUpload.UploadId;

        // Obliczamy liczbę części na podstawie rozmiaru pliku
        const partSize = 7 * 1024 * 1024; // 5 MB na część
        const numberOfParts = Math.ceil(fileSize / partSize);
        const parts = Array.from({ length: numberOfParts }, (_, i) => ({
            partNumber: i + 1,
            url: s3.getSignedUrl("uploadPart", {
                Bucket: BUCKET_NAME,
                Key: params.Key,
                UploadId: uploadId,
                PartNumber: i + 1,
                Expires: 60 * 5, // Link ważny 5 minut
            }),
        }));

        res.status(200).json({ uploadId, parts });
    } catch (err) {
        console.error("Error starting multipart upload:", err.message);
        res.status(500).json({ error: "Error starting multipart upload" });
    }
};

// Generowanie pre-signed URL dla każdej części
const generatePresignedUrl = (uploadId, partNumber, partSize, username, fileName) => {
    const params = {
        Bucket: BUCKET_NAME,
        Key: `test/${fileName}`,
        UploadId: uploadId,
        PartNumber: partNumber,
        Expires: 60 * 5, // Link ważny przez 5 minut
    };

    return s3.getSignedUrl("uploadPart", params);
};

module.exports = { multipart };