const AWS = require("aws-sdk");
const TABLE_NAME = "upload_tickets";
const dynamoDb = new AWS.DynamoDB.DocumentClient();

const getStatus = async (req, res) => {
    const { uploadId } = req.params;

    const params = {
        TableName: TABLE_NAME,
        Key: { uploadId },
    };

    try {
        const result = await dynamoDb.get(params).promise();
        if (!result.Item) return res.status(404).json({ error: "Upload not found" });

        res.status(200).json({
            status: result.Item.status,
            progress: result.Item.progress,
        });
    } catch (err) {
        console.error("Error fetching upload status:", err);
        res.status(500).json({ error: "Error fetching upload status" });
    }
};


const updateUploadStatus = async (uploadId, progress) => {
    const params = {
        TableName: TABLE_NAME,
        Key: { uploadId },
        UpdateExpression: "SET progress = :progress",
        ExpressionAttributeValues: {
            ":progress": progress,
        },
    };
    return dynamoDb.update(params).promise();
};

const updateStatus = async (req, res) => {
    const { uploadId, progress } = req.body;

    if (!uploadId || progress === undefined) {
        return res.status(400).json({ error: "Missing upload details" });
    }

    try {
        await updateUploadStatus(uploadId, progress);
        res.status(200).json({ message: "Progress updated successfully" });
    } catch (err) {
        console.error("Error updating progress:", err);
        res.status(500).json({ error: "Error updating progress" });
    }
};

module.exports = { getStatus, updateStatus };
