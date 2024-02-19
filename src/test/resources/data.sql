INSERT INTO orderStatuses (statusName, description) VALUES('processing', 'test status'), ('dispatched', 'test status');
INSERT INTO orders (userId, totalOrderSum, createdAt) VALUES(1, 20.0, CURRENT_DATE),(2,10,CURRENT_DATE),(1, 11,CURRENT_DATE);
INSERT INTO orderStatusHistory(orderId, statusId, createdAt) VALUES (1,1,CURRENT_DATE),(2,1,CURRENT_DATE),(3,1,CURRENT_DATE),(1,2,CURRENT_DATE);