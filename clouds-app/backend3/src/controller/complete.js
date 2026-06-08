const router = require("../router");
const AWS = require("aws-sdk");
const {saveLogs} = require("./saveLogs");
const verifier = require("../middleware/verifier");
const BUCKET_NAME = "clouds-project-storage";
const s3 = new AWS.S3();

const complete = async (req, res) => {
    const { uploadId, fileName, parts } = req.body;

    if (!uploadId || !fileName || !parts) {
        return res.status(400).json({ error: "Missing upload details" });
    }

    const token = req.headers.authorization || req.headers.Authorization;

    if (!token) {
        await saveLogs({
            timestamp: new Date().toISOString(),
            message: `Authorization token is missing`
        });
        return res.status(401).json({ error: "Authorization token is missing" });
    }

    const payload = await verifier.verify(token);

    try {
        // Przygotowujemy parametry do wywołania completeMultipartUpload
        const completedParts = parts.map((part) => ({
            ETag: part.ETag,
            PartNumber: part.PartNumber, // Numer części powinien być zgodny
        }));
        // Wywołanie completeMultipartUpload
        const params = {
            Bucket: BUCKET_NAME,
            Key: `${payload.username}/${fileName}`,
            UploadId: uploadId,
            MultipartUpload: {
                Parts: completedParts,
            },
        };
        // Zakończenie uploadu
        await s3.completeMultipartUpload(params).promise();
        await saveLogs({
            timestamp: new Date().toISOString(),
            message: `File uploaded ${fileName} for user: ${payload.username}`
        });

        console.log(`File uploaded successfully for user: ${payload.username}`);
        res.status(200).json({ message: "File upload completed successfully" });
    } catch (err) {
        console.error("Error completing multipart upload:", err.message);
        // Jeśli wystąpi błąd, wywołaj abortMultipartUpload, aby anulować częściowy upload
        await s3.abortMultipartUpload({
            Bucket: BUCKET_NAME,
            Key: `test/${fileName}`,
            UploadId: uploadId,
        }).promise();
        console.error("Multipart upload aborted.");
        res.status(500).json({ error: "Error completing multipart upload" });
    }
};

module.exports = {complete};