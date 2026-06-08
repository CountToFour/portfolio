const express = require('express');
const router = express.Router();
const {getList} = require('../controller/getFilesList');
const {downloadFile} = require('../controller/downloadFile');
const {uploadFile} = require('../controller/uploadFile');
const {checkTicketStatus} = require('../controller/checkTicketStatus');
const {multipart} = require('../controller/multipart');
const {complete} = require('../controller/complete');

router.put('/:fileName', uploadFile);
router.get('/download_file/:fileName', downloadFile);
router.get('/list', getList)
router.get('/ticketStatus/:ticketId', checkTicketStatus)
router.put('/multipart/test', multipart);
router.put('/complete/test', complete);

module.exports = router