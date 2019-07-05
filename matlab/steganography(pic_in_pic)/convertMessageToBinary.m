%% 
% .
% Convert the Message into Binary
% The function takes a string message and returns a vector of binary strings 
% of each character.

function binaryMsgList = convertMessageToBinary(msg, fileID)
    disp(['Message: ' msg]);
    disp('Binary Representation: ');
    fprintf(fileID, ['Message: ' msg '\n']);
    fprintf(fileID, 'Binary Representation: \n');
    
    msgAsNums = double(msg);
    binaryMsgList = dec2bin(msgAsNums);
    [rSize, ~] = size(binaryMsgList); %Get number of rows and cols in binaryMsgList
    binaryMsgList = padStringList(binaryMsgList, '0', 8, 0);
    
    %Display each char from msg and its binary represenation
    for x = 1:rSize
        disp([msg(x) '| ' num2str(binaryMsgList(x, :))]);
        fprintf(fileID, [msg(x) '| ' num2str(binaryMsgList(x, :)) '\n']);
    end
    
    fprintf('\n');
    fprintf(fileID, '\n');
end