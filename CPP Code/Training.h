/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                 Ron Cohen               Yaniv Fais                        *
 *****************************************************************************/

#ifndef __TRAINING_H
#define __TRAINING_H


#include "Game.h"
#include "Board.h"
#include "Player.h"
#include "Checkers.h"

#define AVERAGE_HISTORY 10
#define MAX_CONVERGENCE_ATTEMPTS 5000

#include <vector>
#include <sstream>

/**
 * Represent a Machine Learning Game Trainer
 **/
template <size_t N>
class Trainer
{
private:
	typedef vector<double> Weights;

	/**
	 * collection of weights over time used for statistics
	 **/
	vector<Weights> timedWeights;

	/**
	 * add a weight to statistics
	 * @param param parameter index
	 * @param avgWeight weight for parameter
	 */
	void addWeight(int param,double avgWeight)
	{
		static Weights weights(PARAMS_NUM);
		weights[param] = avgWeight;
		if (param == PARAMS_NUM-1) // insert when last parameter
			timedWeights.push_back(weights);
	}

	/**
	 * write weights to Comma Seperated Values file to be used with
	 * spreadsheet such as Microsoft Excel
	 * @param stage stage of weights
	 */
	void writeCSVweights(int stage)
	{
		stringstream name;
		name << "stage" << stage << '_' << (int)N << ".csv";
		ofstream csvFile(name.str().c_str(),ios::trunc);
		if (!csvFile.is_open())
		{
			cerr << "Error:unable to open file " << name.str() << endl;
			return;
		}
		csvFile << "Stage " << stage << " Learning for Dam Ka ! (" << N << ")\n";
		for (int p=0;p<PARAMS_NUM;p++)
		{
			csvFile << ParametersNames[p];
			for (vector<Weights>::const_iterator tWeightsCIter = timedWeights.begin();
				tWeightsCIter != timedWeights.end() ; tWeightsCIter++)
				csvFile << ',' << (*tWeightsCIter)[p] ;
			csvFile << endl;
		}
		csvFile.close();
		timedWeights.clear();
		cout << "stage " << stage << " CSV file written to " << name.str() << endl;
	}

	/**
	 * indicator for writing CSV files
	 */
	bool _writeCSV;

	/**
	 * difference factor
	 */
	float _diffFactor;
public:

	/**
	 * construct trainer
	 * @param writeCSV indicator for writing Comma Seperated Values
	 * @param diff difference factor for updating weights in step
	 */
	Trainer(bool writeCSV,float diff) : _writeCSV(writeCSV),_diffFactor(diff)  {}

	/**
	 * train stages in maximum steps and level of machine player
	 * @param maxSteps maxumum number of steps for update
	 * @param level machine Player level (depth of min-max tree)
	 */
	void trainStages(int maxSteps,int level)
	{
		int whiteTime = 0;
		int blackTime = 0;
		int whiteMoves = 0;
		int blackMoves = 0;
		Game* game = Game::getInstance();
		game->setBoardSize(N);
		game->setLearning(true);

		// Training for all stages
		// Starting from stage 0 (End-Game Stage)
		for (int stage = 0; stage < STAGES; stage++)
		{ 	
			// Initialize weight vectors
			double lastWeights[AVERAGE_HISTORY][PARAMS_NUM];
			for (int p=0; p<AVERAGE_HISTORY; p++)
				for (int w=0; w<PARAMS_NUM; w++)
					lastWeights[p][w] = 0.0;

			for (int j=0; j<PARAMS_NUM;j++)
				addWeight(j,1.0);

			int count = 0; 
			int rows = (int) sqrt(2*N);
			int numberOfPeons = rows * (rows - 2) / 4;

			cout << "Training Stage " <<stage<<" \n";

			// Makes several convergence attempts for the trained stage.
			for (int attempt=0; count<maxSteps; )
			{ 
				if (attempt>MAX_CONVERGENCE_ATTEMPTS)
				{
					cerr << "Training incomplete for stage " << stage 
							<< " ,step " << count << endl;
					break;
				}
				Game* game = Game::getInstance();

				// Set Q-Learning ALPHA
				double alpha = 0.25 * 100 / (100 + attempt);
				Board* board;
				bitset<N> whitePeons, blackPeons;

				// Initialize board
				for (int i=0; i<numberOfPeons; i++)
				{
					whitePeons.set(i);
					blackPeons.set(N-1-i);
				}
				board = new ParamBoard<N>(whitePeons, blackPeons);
				game->setCurrentBoard(board);

				board = game->getCurrentBoard();
				int currentStage = ((ParamBoard<N>*)board)->getStage();

				game->getPlayer(COLOR_WHITE)->setLevel(0);
				game->getPlayer(COLOR_BLACK)->setLevel(0);
				game->clearHistory();

				whiteMoves = 0;

				// First, reach the trained stage, by making random moves
				// on the previous stages.
				while (currentStage > stage && whiteMoves < 1000)
				{ 
					board = game->getCurrentBoard();
					Move* whiteMove = game->getPlayer(COLOR_WHITE)->play();
					if (!whiteMove) break;
					whiteMoves++;
					Board* newBoard = ((ParamBoard<N>*)board)->makeMove(whiteMove, COLOR_WHITE);
					game->setCurrentBoard(newBoard);
					Move* blackMove = game->getPlayer(COLOR_BLACK)->play();
					if (!blackMove) break;
					delete whiteMove;
					delete board;
					board = ((ParamBoard<N>*)newBoard)->makeMove(blackMove, COLOR_BLACK);
					delete blackMove;
					delete newBoard;
					game->setCurrentBoard(board);
					currentStage = ((ParamBoard<N>*)board)->getStage();
				}

				Move* mv = game->getPlayer(COLOR_WHITE)->play();
				if (!mv) continue; // Game ended before the desired stage

				bool trained = false;

				game->getPlayer(COLOR_WHITE)->setLevel(level);
				game->getPlayer(COLOR_BLACK)->setLevel(level);

				whiteMoves = 0;
				// Make some moves in the trained stage
				while (whiteMoves < 50 && currentStage == stage)
				{
					board = game->getCurrentBoard();
					// White player makes a move
					// "Best" known move, or random move.
					int rval = rand();
					int expectedVal = 0;
					if (rval > (RAND_MAX / 4))
						game->getPlayer(COLOR_WHITE)->setLevel(level);
					else
						game->getPlayer(COLOR_WHITE)->setLevel(0);

					Move* whiteMove = game->getPlayer(COLOR_WHITE)->play(&expectedVal);
					game->getPlayer(COLOR_WHITE)->setLevel(level);
					whiteMoves++;
					if (!whiteMove)
						break;
					Board* newBoard = ((ParamBoard<N>*)board)->makeMove(whiteMove, COLOR_WHITE);
					game->setCurrentBoard(newBoard);

					// Black player makes move
					Move* blackMove = game->getPlayer(COLOR_BLACK)->play();


					// On the fist move in the trained stage
					// add 1 to number of convergence attempts
					if (whiteMoves == 1)
					{
						attempt++;
						trained = true;
					}

					if (!blackMove)
						break;
					Board* nextBoard = ((ParamBoard<N>*)newBoard)->makeMove(blackMove, COLOR_BLACK);
					game->getPlayer(COLOR_WHITE)->learn(board, whiteMove, expectedVal, blackMove, alpha);

					delete whiteMove;
					delete blackMove;
					delete board;
					board = nextBoard;

					delete newBoard;
					game->setCurrentBoard(board);
					currentStage = ((ParamBoard<N>*)board)->getStage();
				}
				delete board;
				
				// Ended game in trained stage.
				// Now, modify weight vectors
				if (trained)
				{
					if (attempt > 0)
					{
						double mse = 0.0;
						for (int w=0; w<PARAMS_NUM; w++)
						{
							double currentWeight = game->scaleWeight(game->getPlayer(COLOR_WHITE)->getWeights()[stage][w]);
							double avgWeight = 0.0;
							for (int p=0; p<min(attempt,AVERAGE_HISTORY); p++)
								avgWeight += lastWeights[p%AVERAGE_HISTORY][w];
							avgWeight /= min(attempt,AVERAGE_HISTORY);
							mse += pow(avgWeight - (currentWeight), 2);
							lastWeights[attempt%AVERAGE_HISTORY][w] = currentWeight;
						}
						// If MSE is below the convergence factor, increase step counter
						// and modify weights to be the average
						if (mse < _diffFactor*(stage + 1)*(((double)maxSteps)/(maxSteps+count)))
						{
							count++;
							for (int w=0; w<PARAMS_NUM; w++)
							{
								double avgWeight = 0.0;
								for (int p=0; p<min(attempt,AVERAGE_HISTORY); p++)
									avgWeight += lastWeights[p%AVERAGE_HISTORY][w];
								avgWeight /= min(attempt,AVERAGE_HISTORY);
								game->getPlayer(COLOR_BLACK)->getWeights()[stage][w] = game->scaleWeight(avgWeight);
								addWeight(w,avgWeight);
							}
						}
					}
				}
			}
			for (j=0; j<PARAMS_NUM;j++)
			{
				double avgWeight = 0.0;
				for (int p=0; p<min(attempt,AVERAGE_HISTORY); p++)
					avgWeight += lastWeights[p%AVERAGE_HISTORY][j];
				avgWeight /= min(attempt,AVERAGE_HISTORY);
				game->getPlayer(COLOR_WHITE)->getWeights()[stage][j] = game->scaleWeight(avgWeight);
			}

			if (_writeCSV)
				writeCSVweights(stage);
		}

		ofstream of;
		char name[100];
		sprintf(name, "%s%s%s%d%s", DATA_DIRECTORY,DIR_DELIMITER,WEIGHTS_FILE_PREFIX_NAME, 
			N,WEIGHTS_FILE_EXTENSION);
		of.open(name);
		if (!of.is_open())
		{
			cerr << "Error:unable to open file " << name << endl;
			return;
		}
		for (int i=0; i<STAGES; i++)
			for (int j=0; j<PARAMS_NUM; j++)
				of << game->scaleWeight(game->getPlayer(COLOR_WHITE)->getWeights()[i][j]) << "\n";
		of.close();
		int size = (int)sqrt(N*N);
		cout << "Machince player weights for " <<(int)sqrt(size*2)<<"x" << (int)sqrt(size*2) << " board saved.\n\n";
	}

};

#endif
