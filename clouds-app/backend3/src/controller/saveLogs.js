const AWS = require('aws-sdk');
const s3 = new AWS.S3();
const BUCKET_NAME = 'clouds-project-storage';

const saveLogs = async (logData) => {
    const fileName = `logs/lambda-log.txt`;

    const params = {
        Bucket: BUCKET_NAME,
        Key: fileName,
        Body: `${JSON.stringify(logData, null, 2)}\n`,
        ContentType: 'application/json',
        ACL: 'private',
    };
    try {
        const existingData = await s3.getObject({ Bucket: BUCKET_NAME, Key: fileName }).promise();
        params.Body = existingData.Body + params.Body;
    } catch (error) {
        if (error.code !== 'NoSuchKey') {
            console.error('nieodcztane logi', error);
        }
    }

    try {
        await s3.putObject(params).promise();
        console.log(`zapisany: ${fileName}`);
    } catch (error) {
        console.error('niezapisany:', error);
    }
};

module.exports = { saveLogs };