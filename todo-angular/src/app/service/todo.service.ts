import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http'

import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs';

import { Todo } from '../models/todo';
import { TodoState } from '../models/todoState';

@Injectable({
  providedIn: 'root'
})
export class TodoService {

  private todoUrl = 'api/todo';

  httpOptions = {
    headers: new HttpHeaders({ 
      'Content-Type'  : 'application/json'
    })
  };

  constructor(private http: HttpClient) { }

  /** Todoの一覧を取得する */
  getTodoList(): Observable<Todo[]> {
    return this.http.get<Todo[]>(this.todoUrl).pipe(
      catchError(this.handleError<Todo[]>('getTodoList', []))
    )
  }

  /** IDによりTodoを取得する。見つからなかった場合は404を返却する。 */
  getTodo(id: number): Observable<Todo> {
    const url = `${this.todoUrl}/${id}`;
    return this.http.get<Todo>(url).pipe(
      tap(_ => this.log(`fetched todo id=${id}`)),
      catchError(this.handleError<Todo>(`getTodo id=${id}`))
    );
  }

  /** 進行状態の一覧を取得 */
  getTodoState(): Observable<TodoState[]> {
    const url= `${this.todoUrl}/state`
    return this.http.get<TodoState[]>(url).pipe(
      catchError(this.handleError<TodoState[]>('getTodoState', []))
    )
  }

  /** POST: サーバーに新しいTodoを登録する */
  addTodo(todo: Todo): Observable<Todo> {
    this.setCsrfToken();

    return this.http.post<Todo>(this.todoUrl, todo, this.httpOptions).pipe(
      tap((newTodo: Todo) => this.log(`added todo w/ id=${newTodo.id}`)),
      catchError(this.handleError<Todo>('addTodo'))
    );
  }

  /** PUT: サーバー上でTodoを更新 */
  updateTodo(todo: Todo): Observable<any> {
    this.setCsrfToken();
    const url = `${this.todoUrl}/${todo.id}`

    return this.http.put(url, todo, this.httpOptions).pipe(
      tap(_ => this.log(`update todo id=${todo.id}`)),
      catchError(this.handleError<any>('updateTodo'))
    );
  }

  /** DELETE: サーバーからTodoを削除 */
  deleteTodo(id: number): Observable<Todo> {
    this.setCsrfToken();
    const url = `${this.todoUrl}/${id}`;

    return this.http.delete<Todo>(url, this.httpOptions).pipe(
      tap(_ => this.log(`deleted Todo id=${id}`)),
      catchError(this.handleError<Todo>('deleteTodo'))
    );
  }

  /** csrfトークンをcookieから取得してhttpヘッダに設定 */
  setCsrfToken(): void {
    const csrfToken = document.cookie.match(new RegExp('(^|)' + 'csrf_token' + '=([^;]+)'));
    if(csrfToken) {
      this.httpOptions.headers = new HttpHeaders({
        'Content-Type'  : 'application/json',
        'Csrf-Token'    : `${csrfToken[2]}`
      })
    }
  }

  /** TodoServiceのメッセージをMessageServiceを使って記録 */
  private log(message: string) {
    //this.messageService.add(`TodoService: ${message}`);
    //TODO
    console.log(message);
  }

  /**
   * 失敗したHttp操作を処理します。
   * アプリを持続させます。
   *
   * @param operation - 失敗した操作の名前
   * @param result - observableな結果として返す任意の値
   */
   private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: リモート上のロギング基盤にエラーを送信する
      console.error(error); // かわりにconsoleに出力

      // TODO: ユーザーへの開示のためにエラーの変換処理を改善する
      this.log(`${operation} failed: ${error.message}`);

      // 空の結果を返して、アプリを持続可能にする
      return of(result as T);
    };
  }
}
