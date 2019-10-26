from queue import Queue
import heapq
import time
from math import sqrt
class Node:
    def __init__(self,x,y,pos,elevation):
        self.x = x;
        self.y = y;
        self.pos = pos;
        self.prev = None;
        self.elevation = elevation;
        self.isGoal = False;
        self.isGoalFound = False;
        self.neighbours = []
        self.f = 0;
        self.g = 0;
        self.h = 0;
    
    def resetValues(self):
        self.f = 0;
        self.g = 0;
        self.h = 0;
        self.prev = None;
        self.isGoalFound = False;
        
    def setIsGoal(self):
        self.isGoal = True;
    
    def __lt__(self, other):
        return self.f < other.f
        
    def addNeighbours(self,maxElevation,rows,cols,grid):
        #add neighbors based on the max elevation constraint
        col = self.x;
        row = self.y;
        currentElevation = grid[row][col].elevation

        if col + 1 < cols and (abs(grid[row][col + 1].elevation - currentElevation) <= maxElevation):
            self.neighbours.append(grid[row][col + 1])
            
        if col - 1 >= 0 and (abs(grid[row][col - 1].elevation - currentElevation) <= maxElevation):
            self.neighbours.append(grid[row][col - 1])
            
        if row + 1 < rows and (abs(grid[row + 1][col].elevation - currentElevation) <= maxElevation):
            self.neighbours.append(grid[row + 1][col])
            
        if row - 1 >= 0 and abs(grid[row-1][col].elevation - currentElevation) <= maxElevation:
            self.neighbours.append(grid[row - 1][col])
            
        if row - 1 >=0 and col - 1 >=0 and abs(grid[row - 1][col - 1].elevation - currentElevation) <= maxElevation:
            self.neighbours.append(grid[row - 1][col - 1])
            
        if row + 1 < rows and col - 1 >=0 and abs(grid[row + 1][col - 1].elevation - currentElevation) <= maxElevation:
            self.neighbours.append(grid[row + 1][col - 1])
            
        if row - 1 >= 0 and col + 1  < cols and abs(grid[row - 1][col + 1].elevation - currentElevation) <= maxElevation:
            self.neighbours.append(grid[row - 1][col + 1])
            
        if row + 1 < rows and col + 1  < cols and abs(grid[row + 1][col + 1].elevation - currentElevation) <= maxElevation:
            self.neighbours.append(grid[row + 1][col + 1]) 
        
        return True;

def main():
    #Read the input file and get all the data
    algo,start,grid,goalList = readFileUtil();
    fo = open("output.txt", "w+")
    start_time = time.time()
    if algo.upper() == "BFS":
        BFS(start,goalList);
        #Write result to Output File
        writeFileUtil(grid,goalList,fo); # Do not append  to file. Create a new File
    elif algo.upper() == "UCS":
        UCS(start,goalList);
        #Write result to Output File
        writeFileUtil(grid,goalList,fo); # Do not append  to file. Create a new File
    elif algo.upper() == "A*":
        for i in range(0,len(goalList),2):
            goalNode = grid[goalList[i+1]][goalList[i]]
            AStar(start,goalNode);
            aStarSingleResultWriter(grid,goalList,fo) # Append each result to the file.
            #print("Result Cost:", goalNode.g)
            flushGrid(grid);
    print("--- %s seconds ---" % (time.time() - start_time))
    fo.close();
    
def flushGrid(grid):
    for i in range(0,len(grid)):
        for j in range(0,len(grid[0])):
            grid[i][j].resetValues();
        
def readFileUtil():
    fi = open("input.txt", "r")
    line = fi.readline();
    algo = line.strip();
    line = fi.readline();
    temp = line.split();
    W = int(temp[0]); #Columns
    H = int(temp[1]); #Rows
    line = fi.readline();
    temp = line.split();
    sX = int(temp[0]);
    sY = int(temp[1]);
    line = fi.readline();
    maxElevation = int(line)
    line = fi.readline();
    numberOfTargets = int(line)
    goalList = [] 
    for i in range(0,numberOfTargets):
        line = fi.readline();
        temp = line.split();
        goalList.append(int(temp[0]));
        goalList.append(int(temp[1]));
        
    grid = [None] * H;
    
    for i in range(0,H):
        grid[i] = [None] * W
    
    for i in range(0,H):
        line = fi.readline();
        temp = line.split();
        for j in range(0,W):
            elevation = int(temp[j]);
            pos = (W * i) + j 
            grid[i][j] = Node(j,i,pos,elevation);
            for k in range(0,len(goalList),2):
                if(goalList[k] == j and goalList[k+1] == i):
                    grid[i][j].setIsGoal();
                    break;
                
    for i in range(0,H):
        for j in range(0,W):
            grid[i][j].addNeighbours(maxElevation,H,W,grid); 
    
    #for i in range(0,H):
    #    for j in range(0,W):
    #        print(len(grid[i][j].neighbours), end=" ")
    #    print()
        
    start = grid[sY][sX];
    fi.close();
    return algo,start,grid,goalList;

def writeFileUtil(grid,goalList,fo):
    for i in range(0,len(goalList),2):
        if(grid[goalList[i+1]][goalList[i]]).isGoalFound == True:
            print("Goal Cost",grid[goalList[i+1]][goalList[i]].g);
            temp = grid[goalList[i+1]][goalList[i]];
            res =  ""
            while(temp != None):
                res = " " + str(temp.x) + "," + str(temp.y) + res ;
                #print(res)
                temp = temp.prev;
            res = res.strip();
            fo.write(res + "\n");
        else:
            fo.write("FAIL\n")

def aStarSingleResultWriter(grid,goalList,fo):
    for i in range(0,len(goalList),2):
        if(grid[goalList[i+1]][goalList[i]]).isGoalFound == True:
            print("Goal Cost",grid[goalList[i+1]][goalList[i]].g);
            temp = grid[goalList[i+1]][goalList[i]];
            res =  ""
            while(temp != None):
                res = " " + str(temp.x) + "," + str(temp.y) + res ;
                #print(res)
                temp = temp.prev;
            res = res.strip();
            fo.write(res + "\n");
            return True;
        
    fo.write("FAIL\n")
    return False;

# BFS Algorithm. We cannot stop after finding just a goal.
# Also, we need to consider the goal node as another node in the path
# and hence we need to add its neighbours as well.

def BFS(start,goalList):
    goalCount = 0;
    explored = {}
    explored[start.pos] = 1;
    q = Queue();
    q.put(start);
    
    while not q.empty():
        elem = q.get();
        if elem.isGoal == True:
            elem.isGoalFound = True;
            goalCount +=1
            if goalCount == len(goalList)/2:
                return True;
            
        for i in range(0,len(elem.neighbours)):
            if None == explored.get(elem.neighbours[i].pos):
                elem.neighbours[i].prev = elem
                q.put(elem.neighbours[i])
                explored[elem.neighbours[i].pos] = 1;
    return False;

def UCS(start,goalList):
    goalCount = 0;
    
    frontier = []
    heapq.heapify( frontier )
    
    frontierMap = {}
    explored = {}
        
    frontierMap[start.pos] = start;

    frontier.append(start);
    heapq.heapify( frontier )
    
    while len(frontier) > 0:
        
        elem = heapq.heappop(frontier)
        
        del frontierMap[elem.pos];
        explored[elem.pos] = elem;
        
        if elem.isGoal == True:
            elem.isGoalFound = True;
            goalCount +=1
            if goalCount == len(goalList)/2:
                return True;
        
        #We cannot Stop
        for i in range(0,len(elem.neighbours)):
            #If not in open list or closed list
            tempG = elem.g + getUCSCostRelativeToNodeAndNeighbour(elem,elem.neighbours[i]);
            #print("Path cost to neighbor" + "with POS - "+ str(elem.neighbours[i].pos)+" Cost-"+str(tempG))
            # If not in open nor closed
            if None == explored.get(elem.neighbours[i].pos) and None == frontierMap.get(elem.neighbours[i].pos):
                elem.neighbours[i].g = tempG;
                elem.neighbours[i].prev = elem
                elem.neighbours[i].f = elem.neighbours[i].g
                heapq.heappush(frontier,elem.neighbours[i]) 
                frontierMap[elem.neighbours[i].pos] = elem.neighbours[i]; 
                
            #Already in Open List
            elif None == explored.get(elem.neighbours[i].pos) and None != frontierMap.get(elem.neighbours[i].pos):
                if(tempG < elem.neighbours[i].g):
                    #print("Updating Cost")
                    #Lower cost to reach here
                    elem.neighbours[i].g = tempG
                    elem.neighbours[i].f = elem.neighbours[i].g
                    elem.neighbours[i].prev = elem
                    heapq.heapify( frontier )
                    break;

            #If in explored but not in open
            elif None != explored.get(elem.neighbours[i].pos) and None == frontierMap.get(elem.neighbours[i].pos):
                #print("Unusual Case");
                if tempG < explored.get(elem.neighbours[i].pos).g:
                    closedNode = explored.get(elem.neighbours[i].pos);
                    closedNode.g = tempG;
                    closedNode.f = tempG;
                    closedNode.prev = elem;
                    heapq.heappush(frontier,closedNode) 
                    frontierMap[closedNode.pos] = closedNode
                    del explored.get[elem.neighbours[i].pos]  
                    
    return False;

def getUCSCostRelativeToNodeAndNeighbour(current,neighbor):
    if (((neighbor.x == (current.x + 1)) or ((neighbor.x + 1) == current.x)) and neighbor.y == current.y) or (((neighbor.y == (current.y + 1)) or ((neighbor.y + 1) == current.y)) and neighbor.x == current.x):
        
        return 10;
    return 14;

def AStar(start, goalNode):
    frontier = []
    frontierMap = {}
    explored = {}
    frontierMap[start.pos] = start;
    frontier.append(start);
    heapq.heapify( frontier )

    while len(frontier) > 0:
        elem = heapq.heappop(frontier);
        
        del frontierMap[elem.pos];
        explored[elem.pos] = elem;
        
        if elem.pos == goalNode.pos:
            #print("Found Goal")
            goalNode.isGoalFound = True;
            return True;
        
        else:
            for i in range(0,len(elem.neighbours)):
                #If not in open list or closed list
                #print("UCS Cost",getUCSCostRelativeToNodeAndNeighbour(elem,elem.neighbours[i]));
                #print("getAstar",getAStarCostRelativeToNodeAndNeighbour(elem,elem.neighbours[i]));
                tempG = elem.g + getUCSCostRelativeToNodeAndNeighbour(elem,elem.neighbours[i]) + getAStarCostRelativeToNodeAndNeighbour(elem,elem.neighbours[i]);
                #print(tempG)
                #print("Path cost to neighbor" + "with POS - "+ str(elem.neighbours[i].pos)+" Cost-"+str(tempG))
                # If not in open nor closed
                updatePath = False;
                if None == explored.get(elem.neighbours[i].pos) and None == frontierMap.get(elem.neighbours[i].pos):
                    elem.neighbours[i].g = tempG;
                    elem.neighbours[i].prev = elem;
                    elem.neighbours[i].h = getHeuristic(elem.neighbours[i], goalNode);
                    elem.neighbours[i].f = elem.neighbours[i].g + elem.neighbours[i].h
                    
                    frontier.append(elem.neighbours[i]);
                    
                    
                    heapq.heapify(frontier)

                    frontierMap[elem.neighbours[i].pos] = elem.neighbours[i];
                    
                      
                #Already in Open List    
                elif None == explored.get(elem.neighbours[i].pos) and None != frontierMap.get(elem.neighbours[i].pos):
                    #Now well have to search for this neighbor
                    for k in range(0,len(frontier)):
                        if(frontier[k].pos == elem.neighbours[i].pos):
                            if(tempG < elem.neighbours[i].g):
                                #print("Updating Cost")
                                #Lower cost to reach here
                                frontier[k].g = tempG
                                frontier[k].prev = elem
                                frontier[k].h = getHeuristic(frontier[k], goalNode);
                                frontier[k].f = frontier[k].g + frontier[k].h
                                updatePath = True;
                                break;
                    if updatePath == True:
                        heapq.heapify(frontier)
 
    
    #print("Failure to find path")               
    return False;


def getAStarCostRelativeToNodeAndNeighbour(current,neighbor):
    return abs(current.elevation - neighbor.elevation);

def getHeuristic(current, goal):
    # Euclidean Distance
    return sqrt((current.x - goal.x)**2 + (current.y - goal.y)**2);
    
    #Diagonal distance.
    #http://theory.stanford.edu/~amitp/GameProgramming/Heuristics.html#diagonal-distance
    #dx = abs(current.x - goal.x)
    #dy = abs(current.y - goal.y)
    #return (10 * (dx + dy)) + ((-6) * min(dx,dy));

if __name__ == "__main__":
    main()
