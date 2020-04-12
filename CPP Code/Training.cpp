/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen          Yaniv Fais                        *
 *****************************************************************************/

#include <bitset>
#include <fstream>
#include <string>

#include "time.h"
#include "Training.h"

using namespace std;

/**
 * parameters names
 */
string ParametersNames[] = {		"Pieces Advantage       ",
									"Opponent Liberty       ",
									"Kings                  ",
									"Center Control         ",
									"Kings Center Control   ",
									"Opponent Center Control",
									"Advance                ",
									"Opponent Kings         ",
									"Opponent Guard         ",
									"Cramp                  ",
									"Double Diagonal File   ",
									"Diagonal Moment Value  ",
									"Dyke                   ",
									"Exposure               ",
									"Gap                    ",
									"Hole                   ",
									"Node                   ",
									"Pole                   ",
									"Back Row Control       ",
									"Opponent Hitting       "};


/**
 * display help "screen"
 */
void displayTrainingHelp()
{
	cout	<< "Usage: training [-6][-8][-10] [-level <l>] [-steps <s>] [-diff <d>] [-CSV]  [-h] \n\n"
			<< "Where:\n"
			<< "     [-6 | -8 | -10]: board sizes to train (DEFAULT:all)\n\n"
			<< "     -level <l>: set training level,\n"
			<< "        where l is integral number of machine learning train level\n"
			<< "        (min-max tree depth) (DEFAULT:3)\n"
			<< "     -steps <s>: set maximum number of steps for each stage (DEFAULT:100)\n\n"
			<< "     -diff <d>: differnce factor for weights update in step(DEFAULT:0.4)\n\n"
			<< "     -CSV: write weights as Comma Seperated Values files to be used \n"
			<< "           with spread-sheet software\n\n"
			<< "     -h: To display this help screen\n\n";
}

/**
 * parse training application parameters
 * @param argc number of arguments
 * @param argv arguments
 * @param train18 reference indicator to be set if training 6x6 board
 * @param train32 reference indicator to be set if training 8x8 board
 * @param train50 reference indicator to be set if training 10x10 board
 * @param level reference to be set with level training
 * @param steps reference to be set with number of training steps
 * @param CSV reference indicator to be set if writing CSV files
 * @param diffFactor reference to be set with diff Factor for training
 */
int parseTrainingArguments(int argc,char *argv[],bool&train18,bool&train32,bool&train50,
						   int&level,int&steps,bool&CSV,float& diffFactor)
{
	for (int i=1;i<argc;i++)
	{
		if (strcmp(argv[i],"-6")==0)
			train18 = true;
		else if (strcmp(argv[i],"-8")==0)
			train32 = true;
		else if (strcmp(argv[i],"-10")==0)
			train50 = true;
		else if (strcmp(argv[i],"-level")==0) {
			if (++i<argc)
				level = atoi(argv[i]);
			if ((i>=argc) | (level==0)) {
				cerr << "error: setting level,see help\n";
				return 1;
			}
		}
		else if (strcmp(argv[i],"-steps")==0) {
			if (++i<argc)
				steps = atoi(argv[i]);
			if ((i>=argc) | (steps==0)) {
				cerr << "error: setting steps,see help\n";
				return 2;
			}
		}
		else if  (strcmp(argv[i],"-CSV")==0) {
			CSV = true;
		}
		else if  (strcmp(argv[i],"-diff")==0) {
			if (++i<argc)
				diffFactor = atof(argv[i]);
			if ((i>=argc) | (diffFactor==0)) {
				cerr << "error: setting differnce factor,see help\n";
				return 5;
			}	
		}
		else if  (strcmp(argv[i],"-h")==0) {
			displayTrainingHelp();
			return 6;
		}
		else {
			cerr << "Unknown command: " << argv[i] << ",see help\n";
			return 4;
		}
	}
	return 0;
}

/**
 * Main for training
 * @param argc number of arguments
 * @param argv arguments
 */
int main(int argc, char **argv)
{
	cout << "Dam Ka! Machine Learning Trainer\n\n";
	bool train32 = false, train18 = false,train50 = false,CSV=false;;
	int level = 3;
	int steps = 100;
	float diffFactor = 0.4f;

	// handle input parameters
	if (argc==1)
	{
		displayTrainingHelp();
	}
	else {
		int parse = parseTrainingArguments(argc,argv,train18,train32,train50,
											level,steps,CSV,diffFactor);
		if (parse)
			return parse;
	}

	cout << "\nExisting weight sets might be lost!";
	cout << "\nUse "<< argv[0] <<" -h for more information.\n";
	cout << "\nPress ENTER to start...\n\n";
	getchar();


	if (!train18 & !train32 &!train50)
		train18 = train32 = train50 = true;
	/**
	 * train different board size in level and number of stages
	 */
	if (train18)
	{
		cout << "Training 6x6 Board for " << steps << " Steps in level " << level << endl;
		Trainer<18> t18(CSV,diffFactor);
		t18.trainStages(steps,level);
	}

	if (train32)
	{
		cout << "Training 8x8 Board for " << steps << " Steps in level " << level << endl;
		Trainer<32> t32(CSV,diffFactor);
		t32.trainStages(steps,level);
	}
	if (train50)
	{
		cout << "Training 10x10 Board for " << steps << " Steps in level " << level << endl;
		Trainer<50> t50(CSV,diffFactor);
		t50.trainStages(steps,level);
	}
	cout << "Training is over\n";
	return 0;
}




