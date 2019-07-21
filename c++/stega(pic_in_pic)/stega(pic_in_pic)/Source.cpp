#include <opencv2/opencv.hpp>
#include <iostream>
#include <iomanip>
#include <string>
#include <bitset>
#include <vector>
#include <fstream>
#include <sstream>
#include <math.h>
#include <exception>
#include <experimental/filesystem>
#include <chrono>
using namespace std;
using namespace std::chrono;
using namespace cv;
namespace fs = std::experimental::filesystem;

const char BASE2[] = { "01" };
const vector<string> _bufferDelimiterList = { "1010", "1111", "0101" };
const vector<string> _delimiterList = { "1111", "1010", "1111" };
const string _bufferDelimiterListStr = "101011110101";
const string _delimiterListStr = "111110101111";
const string _padding = "0000";
const int bufferGrouping = 3;

void encode();
void decode();
void completeGroupsForPixelEmbedding(int, vector<string>&);
void addDelimiterToList(vector<string>&, const vector<string>&);
string generateEncodedImage(Mat&, string);
void embedMessage(Mat&, Mat&, Mat&);
void retrieveEncodedImageFromImage(Mat&, Mat&);
bool checkIfFileExists(string);
void chunkString(string, int, vector<string>&);
char* reverseConstString(char const*);
string padLeftWithZeros(string, int);
string padRightWithZeros(string, int);
static string convertBin(int, char* = nullptr, int = 1, bool = true);
//string joinToString(vector<string>&, string);

class WrongMenuChoiceException : public exception
{
public:
	virtual const char* what() const throw()
	{
		return "Invalid Choice! Please enter a number from 1 to 3!";
	}
} wrongChoiceEx;

class OriginalPicTooSmallException : public exception
{
public:
	virtual const char* what() const throw()
	{
		return "Original Picture too small!";
	}
} originPicSmall;

fstream outputStream;

int main()
{
	/*vector<string> test = { "Hello", "all", "the", "people", "of", "the", "world!" };
	cout << joinToString(test, " ");*/

	/*vector<string> vec;
	chunkString("10101111000111", 20, vec);
	
	for (string s : vec)
	{
		cout << s << " ";
	}*/

	do
	{
		try
		{
			string userChoice;

			cout << "Hello! Welcome to Image Encoder (Picture in Picture)!" << endl;
			cout << "Created by: Jose A. Alvarado" << endl;
			cout << "Copyright J.A.A. Productions 2019" << endl;
			cout << endl;
			cout << "Please select an option..." << endl;
			cout << "1. Encode" << endl;
			cout << "2. Decode" << endl;
			cout << "3. Quit" << endl;
			cout << endl;
			cout << "Choice: ";
			getline(cin, userChoice);

			switch (stoi(userChoice, nullptr, 10))
			{
			case 1:
				cout << endl;
				encode();
				break;
			case 2:
				cout << endl;
				decode();
				break;
			case 3:
				cout << "Goodbye!" << endl;
				cout << endl;
				system("pause");
				return 0;
			default:
				throw wrongChoiceEx;
				break;
			}
		}
		catch (exception &e)
		{
			cout << e.what() << endl << endl;
			cv::destroyAllWindows();
		}
	} while (true);

	cout << endl;
	system("pause");
	return 0;
}

void encode()
{
	string originalPath;
	string embeddedImgPath;
	string newPath;
	string encodedImgSaveLoc;

	outputStream = fstream("debugEn.txt", ios::out);

	cout << endl;
	cout << "Enter the location of the image you wish to embed: ";
	getline(cin, embeddedImgPath);
	cout << endl;

	if (!checkIfFileExists(embeddedImgPath))
		throw exception("Picture does not exist!");

	cout << endl;
	cout << "Enter the location of the image to be encoded: ";
	getline(cin, originalPath);
	cout << endl;

	if (!checkIfFileExists(originalPath))
		throw exception("Picture does not exist!");

	cout << "Enter the location where you wish to save the new encoded image: ";
	getline(cin, newPath);
	cout << endl;
	cout << "Encoding Image..." << endl;

	Mat img = imread(originalPath);
	Mat imgToHide = imread(embeddedImgPath);
	Mat newImg;
	embedMessage(img, imgToHide, newImg);
	encodedImgSaveLoc = generateEncodedImage(newImg, newPath);
	cout << endl;

	outputStream.flush();
	outputStream.close();

	cout << "Encoded Image Saved At: " << encodedImgSaveLoc << endl;
	cout << "Image encoded successfully!" << endl;

	/*namedWindow("Original_Image", WINDOW_NORMAL);
	imshow("Original_Image", img);

	namedWindow("Hidden_Image", WINDOW_NORMAL);
	imshow("Hidden_Image", imgToHide);

	namedWindow("Encoded_Image", WINDOW_NORMAL);
	imshow("Encoded_Image", newImg);
	waitKey(0);*/

	cout << endl << endl;
}

void decode()
{
	string picPath;
	string retrievedImagePath;
	string decodedImgSaveLoc;

	outputStream = fstream("debugRet.txt", ios::out);

	cout << "Enter the location of the encoded image: ";
	getline(cin, picPath);
	cout << endl;

	if (!checkIfFileExists(picPath))
		throw exception("Picture does not exist!");

	cout << "Enter the location where you wish to save the new retrieved image: ";
	getline(cin, retrievedImagePath);
	cout << endl;
	cout << "Retrieving Image...";

	Mat encodedImg = imread(picPath);
	Mat retImg;
	retrieveEncodedImageFromImage(encodedImg, retImg);
	decodedImgSaveLoc = generateEncodedImage(retImg, retrievedImagePath);
	cout << endl;

	outputStream.flush();
	outputStream.close();

	cout << "Retrieved Image Saved At: " << decodedImgSaveLoc << endl;
	cout << "Image decoded successfully!" << endl;

	/*namedWindow("Encoded_Image", WINDOW_NORMAL);
	imshow("Encoded_Image", encodedImg);

	namedWindow("Retrieved_Image", WINDOW_NORMAL);
	imshow("Retrieved_Image", retImg);
	waitKey(0);*/

	cout << endl << endl;
}

string generateEncodedImage(Mat& image, string picturePath)
{
	char full[_MAX_PATH];
	_fullpath(full, picturePath.c_str(), _MAX_PATH);
	string pngPath = picturePath + ".png";

	/*vector<int> compression_params;
	compression_params.push_back(IMWRITE_PNG_COMPRESSION);
	compression_params.push_back(0);*/

	if (checkIfFileExists(picturePath))
		remove(picturePath.c_str());

	imwrite(pngPath, image);
	rename(pngPath.c_str(), picturePath.c_str());

	return full;
}

bool checkIfFileExists(string filename)
{
	char full[_MAX_PATH];
	_fullpath(full, filename.c_str(), _MAX_PATH);

	fs::path filePath = fs::path(full);

	return fs::exists(filePath);
}

void completeGroupsForPixelEmbedding(int input, vector<string>& completeList)
{
	string binInput = convertBin(input);

	int paddingAmt = (int)ceil(binInput.length() / 4.0) * 4;
	int currGroupAmt = paddingAmt / 4;
	int targetGroupAmt = (int) ceil(currGroupAmt / (bufferGrouping + 0.0)) * bufferGrouping;
	int neededGroupAmt = targetGroupAmt - currGroupAmt;

	int overallGroupPadding = paddingAmt;
	if (overallGroupPadding % bufferGrouping != 0)
		overallGroupPadding = (int) ceil(overallGroupPadding / (bufferGrouping + 0.0) + neededGroupAmt) * bufferGrouping;

	string paddedBinInput = padLeftWithZeros(binInput, overallGroupPadding);
	chunkString(paddedBinInput.c_str(), 4, completeList);

	return;
}

void addDelimiterToList(vector<string> &list, const vector<string> &delimiterList)
{
	for (int i = 0; i < delimiterList.size(); i++)
	{
		list.push_back(delimiterList[i]);
	}
}

void embedMessage(Mat &picToEncode, Mat &picToHide, Mat &encodedImg)
{
	milliseconds startTime = duration_cast<milliseconds>(system_clock::now().time_since_epoch());

	int originalPicWidth = picToEncode.cols;
	int originalPicHeight = picToEncode.rows;
	int originalPicPixelArea = originalPicHeight * originalPicWidth;

	int picToHideWidth = picToHide.cols;
	int picToHideHeight = picToHide.rows;
	int picToHidePixelArea = picToHideHeight * picToHideWidth;

	encodedImg = Mat(picToEncode);

	vector<string> picToHidePixelAreaBinList;
	completeGroupsForPixelEmbedding(picToHideWidth, picToHidePixelAreaBinList);
	addDelimiterToList(picToHidePixelAreaBinList, _bufferDelimiterList);
	completeGroupsForPixelEmbedding(picToHideHeight, picToHidePixelAreaBinList);

	int totalPixelsForBuffer = picToHidePixelAreaBinList.size() / bufferGrouping;

	if (picToHidePixelArea > (originalPicPixelArea - totalPixelsForBuffer))
		throw originPicSmall;

	outputStream << "Image in Binary..." << endl << endl;

	int pixelsLeftForBuffer = totalPixelsForBuffer;
	bool insertDelimiter = false;
	int picToHideYIndex = -2;
	int picToHideXIndex = 0;
	bool shouldEmbedPixel = true;

	for (int xIndex = 0; xIndex < originalPicHeight; xIndex++)
	{
		for (int yIndex = 0; yIndex < originalPicWidth; yIndex++)
		{
			Vec3b originalImgPixel = picToEncode.at<Vec3b>(xIndex, yIndex);

			if (shouldEmbedPixel)
			{
				vector <string> originalImgPixelRed;
				vector <string> originalImgPixelGreen;
				vector <string> originalImgPixelBlue;
				vector<string> originalImgRgbBin;
				string originalImgRgbBinStr;

				chunkString(bitset<8>(originalImgPixel[2]).to_string(), 4, originalImgPixelRed);
				chunkString(bitset<8>(originalImgPixel[1]).to_string(), 4, originalImgPixelGreen);
				chunkString(bitset<8>(originalImgPixel[0]).to_string(), 4, originalImgPixelBlue);

				string originalImgPixelRedStr = originalImgPixelRed[0] + " " + originalImgPixelRed[1];
				string originalImgPixelGreenStr = originalImgPixelGreen[0] + " " + originalImgPixelGreen[1];
				string originalImgPixelBlueStr = originalImgPixelBlue[0] + " " + originalImgPixelBlue[1];
				originalImgRgbBinStr = bitset<8>(originalImgPixel[2]).to_string() + " | " + bitset<8>(originalImgPixel[1]).to_string() + " | " + bitset<8>(originalImgPixel[0]).to_string();

				outputStream << "Original Image Pixel (x:" << to_string(xIndex) << ", y:" << to_string(yIndex) << "): R - " << originalImgPixelRedStr << " G - " << originalImgPixelGreenStr << " B - " << originalImgPixelBlueStr;
				outputStream << endl;
				outputStream << "Original Image Pixel (Full): " << originalImgRgbBinStr;

				outputStream << endl;

				vector<string> picToHidePixelRed;
				vector<string> picToHidePixelGreen;
				vector<string> picToHidePixelBlue;
				string picToHidePixelRedStr;
				string picToHidePixelGreenStr;
				string picToHidePixelBlueStr;
				string picToHideRgbBinStr;

				if (pixelsLeftForBuffer > 0)
				{
					int startIndex = picToHidePixelAreaBinList.size() - (pixelsLeftForBuffer * bufferGrouping);

					picToHidePixelRed = { picToHidePixelAreaBinList[startIndex], _padding };
					picToHidePixelGreen = { picToHidePixelAreaBinList[startIndex + 1], _padding };
					picToHidePixelBlue = { picToHidePixelAreaBinList[startIndex + 2], _padding };

					picToHidePixelRedStr = picToHidePixelRed[0] + " " + picToHidePixelRed[1];
					picToHidePixelGreenStr = picToHidePixelGreen[0] + " " + picToHidePixelGreen[1];
					picToHidePixelBlueStr = picToHidePixelBlue[0] + " " + picToHidePixelBlue[1];
					picToHideRgbBinStr = picToHidePixelRedStr + " | " + picToHidePixelGreenStr + " | " + picToHidePixelBlueStr;

					outputStream << "BUFFER " << endl;

					pixelsLeftForBuffer--;

					if (pixelsLeftForBuffer == 0)
						insertDelimiter = true;
				}
				else if (insertDelimiter)
				{
					picToHidePixelRed = { _delimiterList[0], _delimiterList[0] };
					picToHidePixelGreen = { _delimiterList[1], _delimiterList[1] };
					picToHidePixelBlue = { _delimiterList[2], _delimiterList[2] };

					picToHidePixelRedStr = picToHidePixelRed[0] + " " + picToHidePixelRed[1];
					picToHidePixelGreenStr = picToHidePixelGreen[0] + " " + picToHidePixelGreen[1];
					picToHidePixelBlueStr = picToHidePixelBlue[0] + " " + picToHidePixelBlue[1];
					picToHideRgbBinStr = picToHideRgbBinStr = picToHidePixelRedStr + " | " + picToHidePixelGreenStr + " | " + picToHidePixelBlueStr;

					outputStream << "DELIMITER " << endl;
					insertDelimiter = false;
				}
				else
				{
					vector<string> picToHidRgbBin;

					Vec3b picToHidePixel = picToHide.at<Vec3b>(picToHideXIndex, picToHideYIndex);

					chunkString(bitset<8>(picToHidePixel[2]).to_string(), 4, picToHidePixelRed);
					chunkString(bitset<8>(picToHidePixel[1]).to_string(), 4, picToHidePixelGreen);
					chunkString(bitset<8>(picToHidePixel[0]).to_string(), 4, picToHidePixelBlue);

					picToHidePixelRedStr = picToHidePixelRed[0] + " " + picToHidePixelRed[1];
					picToHidePixelGreenStr = picToHidePixelGreen[0] + " " + picToHidePixelGreen[1];
					picToHidePixelBlueStr = picToHidePixelBlue[0] + " " + picToHidePixelBlue[1];
					picToHideRgbBinStr = bitset<8>(picToHidePixel[2]).to_string() + " | " + bitset<8>(picToHidePixel[1]).to_string() + " | " + bitset<8>(picToHidePixel[0]).to_string();
				}

				outputStream << "Picture To Hide Image Pixel (x:" << to_string(picToHideXIndex) << ", y:" << to_string(picToHideYIndex) << "): R - " << picToHidePixelRedStr << " G - " << picToHidePixelGreenStr << " B - " << picToHidePixelBlueStr;
				outputStream << endl;
				outputStream << "Picture To Hide Image Pixel (Full): " << picToHideRgbBinStr;

				outputStream << endl;

				/*vector<string> newImgPixelRed = { originalImgPixelRed[0], picToHidePixelRed[0] };
				vector<string> newImgPixelGreen = { originalImgPixelGreen[0], picToHidePixelGreen[0] };
				vector<string> newImgPixelBlue = { originalImgPixelBlue[0], picToHidePixelBlue[0] };*/
				string newImgPixelRedStr = originalImgPixelRed[0] + picToHidePixelRed[0];
				string newImgPixelGreenStr = originalImgPixelGreen[0] + picToHidePixelGreen[0];
				string newImgPixelBlueStr = originalImgPixelBlue[0] + picToHidePixelBlue[0];
				string newImgRgbBinStr = newImgPixelRedStr + " | " + newImgPixelGreenStr + " | " + newImgPixelBlueStr;

				outputStream << "New Image Pixel (x:" << to_string(xIndex) << ", y:" << to_string(yIndex) << "): R - " << newImgPixelRedStr << " G - " << newImgPixelGreenStr << " B - " << newImgPixelBlueStr;
				outputStream << endl;
				outputStream << "New Image Pixel (Full): " << newImgRgbBinStr;

				outputStream << endl << endl;

				encodedImg.at<Vec3b>(xIndex, yIndex) = Vec3b(stoi(newImgPixelBlueStr, nullptr, 2), stoi(newImgPixelGreenStr, nullptr, 2), stoi(newImgPixelRedStr, nullptr, 2));
			}
			else
			{
				//encodedImg.at<Vec3b>(xIndex, yIndex) = originalImgPixel;
				goto outer;
			}

			if (picToHideXIndex + 1 == picToHideHeight && picToHideYIndex + 1 == picToHideWidth)
			{
				insertDelimiter = true;
				picToHideXIndex = -100;
			}
			else if (picToHideXIndex < 0)
				shouldEmbedPixel = false;

			if (picToHideYIndex + 1 == picToHideWidth)
			{
				picToHideYIndex = 0;
				picToHideXIndex++;
			}
			else if (pixelsLeftForBuffer == 0)
				picToHideYIndex++;
		}
	}

outer:
	outputStream.flush();

	milliseconds endTime = duration_cast<milliseconds>(system_clock::now().time_since_epoch());
	double timeDiff = (endTime.count() - startTime.count()) / 1000.0;
	cout << "\nEncoding Time: " << to_string(timeDiff) << " secs\n\n";
	return;
}

void retrieveEncodedImageFromImage(Mat &encodedImg, Mat &retrievedImg)
{
	milliseconds startTime = duration_cast<milliseconds>(system_clock::now().time_since_epoch());

	int retrievedImgHeight = 100;
	int retrievedImgWidth = 100;

	int encodedImgHeight = encodedImg.rows;
	int encodedImgWidth = encodedImg.cols;

	retrievedImg = Mat(retrievedImgHeight, retrievedImgWidth, CV_8UC3, Scalar(0, 0, 255));

	bool bufferRetrieved = false;
	string retrievedBuffer = "";
	int retrievedImgYIndex = 0;
	int retrievedImgXIndex = 0;

	for (int xIndex = 0; xIndex < encodedImgHeight; xIndex++)
	{
		for (int yIndex = 0; yIndex < encodedImgWidth; yIndex++)
		{
			Vec3b encodedImgPixel = encodedImg.at<Vec3b>(xIndex, yIndex);

			vector<string> encodedImgPixelRed;
			vector<string> encodedImgPixelGreen;
			vector<string> encodedImgPixelBlue;
			string encodedImgPixelRedStr;
			string encodedImgPixelGreenStr;
			string encodedImgPixelBlueStr;
			string encodedImgRgbBinStr;

			chunkString(bitset<8>(encodedImgPixel[2]).to_string(), 4, encodedImgPixelRed);
			chunkString(bitset<8>(encodedImgPixel[1]).to_string(), 4, encodedImgPixelGreen);
			chunkString(bitset<8>(encodedImgPixel[0]).to_string(), 4, encodedImgPixelBlue);

			encodedImgPixelRedStr = encodedImgPixelRed[0] + " " + encodedImgPixelRed[1];
			encodedImgPixelGreenStr = encodedImgPixelGreen[0] + " " + encodedImgPixelGreen[1];
			encodedImgPixelBlueStr = encodedImgPixelBlue[0] + " " + encodedImgPixelBlue[1];
			encodedImgRgbBinStr = encodedImgPixelRedStr + " | " + encodedImgPixelGreenStr + " | " + encodedImgPixelBlueStr;

			outputStream << "Encoded Image Pixel (x:" << to_string(xIndex) << ", y:" << to_string(yIndex) << "): R - " << encodedImgPixelRedStr << " G - " << encodedImgPixelGreenStr << " B - " << encodedImgPixelBlueStr;
			outputStream << endl;
			outputStream << "Encoded Image Pixel (Full): " << encodedImgRgbBinStr;
			outputStream << endl;

			string retrievedBin = encodedImgPixelRed[1] + encodedImgPixelGreen[1] + encodedImgPixelBlue[1];

			if (!bufferRetrieved)
			{
				if (retrievedBin == _delimiterListStr)
				{
					bufferRetrieved = true;
					retrievedImgHeight = stoi(retrievedBuffer, nullptr, 2);

					outputStream << "Width: " << to_string(retrievedImgWidth) << " Height: " << to_string(retrievedImgHeight) << endl << endl;

					retrievedImg = Mat(retrievedImgHeight, retrievedImgWidth, CV_8UC3, Scalar(0, 0, 255));
				}
				else if (retrievedBin == _bufferDelimiterListStr)
				{
					retrievedImgWidth = stoi(retrievedBuffer, nullptr, 2);
					retrievedBuffer = "";
				}
				else
				{
					retrievedBuffer += retrievedBin;
				}
			}
			else
			{
				if (retrievedBin == _delimiterListStr)
					goto outer;
				else
				{
					/*vector<string> retrievedImgPixelRed = { encodedImgPixelRed[1], _padding };
					vector<string> retrievedImgPixelGreen = { encodedImgPixelGreen[1], _padding };
					vector<string> retrievedImgPixelBlue = { encodedImgPixelBlue[1], _padding };*/
					string retrievedImgPixelRedStr = encodedImgPixelRed[1] + _padding;
					string retrievedImgPixelGreenStr = encodedImgPixelGreen[1] + _padding;
					string retrievedImgPixelBlueStr = encodedImgPixelBlue[1] + _padding;
					string retrievedImgRgbBinStr = retrievedImgPixelRedStr + " | " + retrievedImgPixelGreenStr + " | " + retrievedImgPixelBlueStr;

					outputStream << "Retrieved Image Pixel (x:" << to_string(retrievedImgXIndex) << ", y:" << to_string(retrievedImgYIndex) << "): R - " << retrievedImgPixelRedStr << " G - " << retrievedImgPixelGreenStr << " B - " << retrievedImgPixelBlueStr;
					outputStream << endl;
					outputStream << "Retrieved Image Pixel (Full): " << retrievedImgRgbBinStr;

					outputStream << endl << endl;

					retrievedImg.at<Vec3b>(retrievedImgXIndex, retrievedImgYIndex) = Vec3b(stoi(retrievedImgPixelBlueStr, nullptr, 2), stoi(retrievedImgPixelGreenStr, nullptr, 2), stoi(retrievedImgPixelRedStr, nullptr, 2));
				}

				if (retrievedImgYIndex + 1 == retrievedImgWidth)
				{
					retrievedImgYIndex = 0;
					retrievedImgXIndex++;
				}
				else
					retrievedImgYIndex++;
			}
		}
	}

	outer:
	outputStream.flush();

	milliseconds endTime = duration_cast<milliseconds>(system_clock::now().time_since_epoch());
	double timeDiff = (endTime.count() - startTime.count()) / 1000.0;
	cout << "\nDecoding Time: " << to_string(timeDiff) << " secs\n\n";

	return;
}

void chunkString(string s, int size, vector<string> &chunkedList)
{
	int startIndex = 0;
	int endIndex = size;

	while (startIndex != endIndex)
	{
		chunkedList.push_back(s.substr(startIndex, size));
		startIndex += size;
		
		if (endIndex < s.length())
			endIndex += size;
	}
}

char* reverseConstString(char const* str)
{
	// find length of string 
	int n = strlen(str);

	// create dynamic pointer char array 
	char *rev = new char[n + 1];

	// copy of string to ptr array 
	strcpy_s(rev, n + 1, str);

	// Swap character starting from two 
	// corners 
	for (int i = 0, j = n - 1; i < j; i++, j--)
		swap(rev[i], rev[j]);

	// return pointer of reversed string 
	return rev;
}

string padLeftWithZeros(string s, int amount)
{
	stringstream ss;
	string paddedStr;

	ss << "" << setfill('0') << setw(amount) << s;
	getline(ss, paddedStr);

	return paddedStr;
}

string padRightWithZeros(string s, int amount)
{
	stringstream ss;
	string paddedStr;

	ss << s << setw(amount - s.length()) << setfill('0') << "";
	getline(ss, paddedStr);

	return paddedStr;
}

string convertBin(int num, char* buildNum, int placeVal, bool initial)
{
	int diff = num;
	int pVal = placeVal;
	int pCount = log2(pVal);
	char rem;

	if (buildNum == nullptr)
	{
		buildNum = new char[1];
		buildNum[0] = NULL;
	}

	char* tempPtr = buildNum;

	if (strlen(buildNum) == 0)
	{
		while (pVal < num)
		{
			pVal *= 2;
		}

		if (pVal > num)
		{
			pVal /= 2;
		}
	}
	else if (pVal != 0)
		pVal /= 2;

	pCount = log2(pVal);

	if (pVal != 0)
	{
		if (pVal > diff)
			rem = BASE2[0];
		else
		{
			diff -= pVal;
			rem = BASE2[1];
		}

		*tempPtr = rem;
		tempPtr++;
		convertBin(diff, tempPtr, pVal, false);
	}
	else
	{
		pCount = 0;

		if (strlen(buildNum) == 0)
			buildNum[0] = '0';
	}

	if (initial)
	{
		buildNum[pCount + 1] = NULL;
		return string(buildNum);
	}

	return "";
}