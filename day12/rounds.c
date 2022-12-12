#include <limits.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_LINES 100

size_t nb_cols = 0, nb_rows = 0, nb_nodes = 0, src = 0, dest = 0;
unsigned int *all_a;

int** readgraph(char* filename) {
    FILE * fp;
    char * line = NULL;
    size_t len = 0;
    ssize_t read;

    fp = fopen(filename, "r");
    if (fp == NULL) {
    	printf("Error reading file !\n");
    	return NULL;
    }

    char *nodes;
    char *p_nodes;

    nb_rows = nb_cols = 0;
    read = getline(&line, &len, fp);
    if (read != -1) {
    	do {
    		if (nb_rows >= MAX_LINES)
    			exit(-1); // too many lines, increase MAX_LINES

    		if (nb_cols == 0) { // assume the field is regular
    			nb_cols = read - 1;
    			nodes = malloc(MAX_LINES * nb_cols * sizeof(char));
    			p_nodes = nodes;
    		}
    		strncpy(p_nodes, line, nb_cols);
    		p_nodes += nb_cols;
    		nb_rows++;
    	} while ((read = getline(&line, &len, fp)) != -1);
    }
    fclose(fp);
    if (line) free(line);

    nb_nodes = nb_rows * nb_cols;

    /* allocate graph and 'a' vector */
    int **graph = malloc(nb_nodes * sizeof(int *));
    all_a = malloc(nb_nodes * sizeof(size_t));
    for (int i = 0 ; i < nb_nodes; i++)
    	graph[i] = malloc(nb_nodes * sizeof(int)), all_a[i] = -1;

    /* identify start and dest, replace by real values, identify 'a's */
    unsigned int i_a = 0;
    for (int i = 0; i < nb_nodes; i++) {
    	int col = i % nb_cols, row = i / nb_cols;
    	char current = nodes[row * nb_cols + col];

    	if (current == 'S') {
    		current = 'a';
    		src = i, nodes[row * nb_cols + col] = 'a';
    	} else if (current == 'E')
    		dest = i, nodes[row * nb_cols + col] = 'z';

    	if (current == 'a')
    		all_a[i_a++] = i;
    }

    /* fill-in graph */
    for (int i = 0; i < nb_nodes; i++) {
    	int col = i % nb_cols, row = i / nb_cols;
    	char current = nodes[row * nb_cols + col], next_char = current + 1;

    	if ((col - 1 >= 0) && (nodes[row * nb_cols + col - 1] <= next_char))	graph[i][i-1] = 1;
    	if ((col + 1 < nb_cols) && (nodes[row * nb_cols + col + 1] <= next_char)) graph[i][i+1] = 1;
    	if ((row - 1 >= 0) && (nodes[(row - 1) * nb_cols + col] <= next_char)) graph[i][i-nb_cols] = 1;
    	if ((row + 1 < nb_rows) && (nodes[(row + 1) * nb_cols + col] <= next_char)) graph[i][i+nb_cols] = 1;
    }

    free(nodes);
    return graph;
}

int minDistance(int dist[], bool * sptSet, int nb_nodes)
{
    int min = INT_MAX, min_index;

    for (int v = 0; v < nb_nodes; v++)
        if (sptSet[v] == false && dist[v] <= min)
            min = dist[v], min_index = v;

    return min_index;
}

int *dijkstra(int **graph, int src, int nb_nodes)
{
    int * dist = malloc(nb_nodes * sizeof(int));
    bool * sptSet = malloc(nb_nodes * sizeof(bool));

    for (int i = 0; i < nb_nodes; i++)
        dist[i] = INT_MAX, sptSet[i] = false;

    dist[src] = 0;
    for (int count = 0; count < nb_nodes - 1; count++) {
        int u = minDistance(dist, sptSet, nb_nodes);
        sptSet[u] = true;

        for (int v = 0; v < nb_nodes; v++)
            if (!sptSet[v] && graph[u][v]
                && dist[u] != INT_MAX
                && dist[u] + graph[u][v] < dist[v])
                dist[v] = dist[u] + graph[u][v];
    }
    free(sptSet);

    return dist;
}

// driver's code
int main()
{
	int** graph = readgraph("input.txt");

    // Round 1
    int *dist = dijkstra(graph, src, nb_nodes);
    printf("Round 1: %d\n", dist[dest]);

    // Round 2
    unsigned int node = 0;
    int i_a = 0;
    int min = INT_MAX;
    while ((node = all_a[i_a++]) != -1) {
    	dist = dijkstra(graph, node, nb_nodes);
    	if (dist[dest] < min) {
    		min = dist[dest];
    	}
    }
    printf("Round 2: %d\n", min);

    free(dist);
    return 0;
}
