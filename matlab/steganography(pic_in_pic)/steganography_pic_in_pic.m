% Hello! Welcome to Image Encoder/Decoder (Data in Picture)!
% Created By: Jose A. Alvarado
% 
% Copyright J.A.A. Productions 2019
% 
% % Initializations

clear
clc
global BUFFER_DELIMITER;
global DELIMITER;
global BUFFER_GROUPING;
global PADDING;
BUFFER_DELIMITER = '101011110101';
DELIMITER = '111110101111';
PADDING = '0000';
BUFFER_GROUPING = 3;

continueRun = true;
while continueRun
    try
        userChoice = input('Please select an option...\n1. Encode\n2. Decode\n3. Quit\n\nChoice: ');
        switch(userChoice)
            case 1
                fprintf('\n');
                encode();
            case 2
                fprintf('\n');
                decode();
            case 3
                disp('Goodbye!');
                continueRun = false;
                break;
        end
    catch exception
        %fprintf([exception.message '\n\n']);
        rethrow(exception);
    end
end

fclose('all');
clear