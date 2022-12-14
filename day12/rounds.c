#include <limits.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_LINES 100

size_t nb_cols = 0, nb_rows = 0, nb_nodes = 0, src = 0, dest = 0;
unsigned int *all_a;

/* ------------------------------------------------------------------------- */

int** readgraph(char* filename) {
    FILE * fp;
    char * line = NULL;
    size_t len = 0;
    ssize_t read;

    fp = fopen(filename, "r");
    if (fp == NULL) {
        printf("Error reading file !\n");
        exit(-1);
    }

    char *nodes;
    char *p_nodes;

    /* fill nodes as an array of chars */
    nb_rows = nb_cols = 0;
    while ((read = getline(&line, &len, fp)) != -1) {
        if (nb_rows++ >= MAX_LINES)
            exit(-2); // too many lines, increase MAX_LINES

        if (nb_cols == 0) { // assume the field is regular
            nb_cols = read - 1;
            p_nodes = nodes = malloc(MAX_LINES * nb_cols * sizeof(char));
        }

        strncpy(p_nodes, line, nb_cols);
        p_nodes += nb_cols;
     }
    fclose(fp);
    if (line) free(line);

    nb_nodes = nb_rows * nb_cols;

    /* allocate graph and 'a' vector */
    int **graph = malloc(nb_nodes * sizeof(int *));
    all_a = malloc(nb_nodes * sizeof(int));
    for (int i = 0 ; i < nb_nodes; i++) // TODO: initialize to INT_MAX ?
        graph[i] = malloc(nb_nodes * sizeof(int)), all_a[i] = -1;

    /* identify start and dest, replace by the real values, identify 'a's */
    for (unsigned int i_a = 0, i = 0; i < nb_nodes; i++) {
        char current = nodes[i];

        if (current == 'S')
            src = i, nodes[i] = current = 'a';
        else if (current == 'E')
            dest = i, nodes[i] = current = 'z';

        if (current == 'a')
            all_a[i_a++] = i;
    }

    /* fill-in graph */
    for (int i = 0; i < nb_nodes; i++) {
        int col = i % nb_cols, row = i / nb_cols;
        char next_char = nodes[i] + 1;

        for (int n = -1; n < 2; n += 2) {
            if ((col + n >= 0 && col + n < nb_cols) && (nodes[i + n] <= next_char))
                graph[i][i + n] = 1;
            if ((row + n >= 0 && row + n < nb_rows) && (nodes[i + n * nb_cols] <= next_char))
                graph[i][i + n * nb_cols] = 1;
        }
    }

    free(nodes);
    return graph;
}

/* ------------------------------------------------------------------------- */

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

/* ------------------------------------------------------------------------- */

int main()
{
    int** graph = readgraph("input.txt");

    /* Round 1 */
    int *dist = dijkstra(graph, src, nb_nodes);
    printf("Round 1: %d\n", dist[dest]);
    free(dist);

    /* Round 2 */
    for (int i = 0; i < nb_nodes; i++) { /* Invert graph */
    	for (int j = i + 1; j < nb_nodes; j++) {
    		int tmp = graph[i][j];
    		graph[i][j] = graph[j][i];
    		graph[j][i] = tmp;
    	}
    }
    dist = dijkstra(graph, dest, nb_nodes); /* Search from dest */
    unsigned int node = 0, i_a = 0, min = INT_MAX;
    while ((node = all_a[i_a++]) != -1) {
        if (dist[node] < min)
            min = dist[node];
    }
    free(dist);
    printf("Round 2: %d\n", min);

    /* free remaining memory */
    for (int i = 0; i < nb_nodes; i++)
        free(graph[i]);
    free(graph);
    free(all_a);

    return 0;
}
