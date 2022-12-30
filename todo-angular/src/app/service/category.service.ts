import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http'

import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs';

import { Category } from '../models/category';
import { CategoryColor } from '../models/color';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  private categoryUrl = 'api/todo/category';
  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(private http: HttpClient) { }

  /** カテゴリの一覧を取得 */
  getCategoryList(): Observable<Category[]> {
    return this.http.get<Category[]>(this.categoryUrl).pipe(
      catchError(this.handleError<Category[]>('getCategoryList', []))
    )
  }

  /** IDによりCategoryを取得する。見つからなかった場合は404を返却する。 */
  getCategory(id: number): Observable<Category> {
    const url = `${this.categoryUrl}/${id}`;
    return this.http.get<Category>(url).pipe(
      tap(_ => this.log(`fetched category id=${id}`)),
      catchError(this.handleError<Category>(`getCategory id=${id}`))
    );
  }

  /** カテゴリカラーの一覧を取得 */
  getCategoryColorList(): Observable<CategoryColor[]> {
    const url = `${this.categoryUrl}/color`;
    return this.http.get<CategoryColor[]>(url).pipe(
      tap(_ => this.log(`fetched category color list`)),
      catchError(this.handleError<CategoryColor[]>(`getCategoryColorList`, []))
    )
  }

  /** POST: サーバーに新しいCategoryを登録する */
  addCategory(category: Category): Observable<Category> {
    this.setCsrfToken();

    return this.http.post<Category>(this.categoryUrl, JSON.stringify(category), this.httpOptions).pipe(
      tap((newCategory: Category) => this.log(`added category w/ id=${newCategory.id}`)),
      catchError(this.handleError<Category>('addCategory'))
    );
  }

  /** PUT: サーバー上でCategoryを更新 */
  updateCategory(category: Category): Observable<any> {
    this.setCsrfToken();
    const url = `${this.categoryUrl}/${category.id}`
    
    return this.http.put(url, category, this.httpOptions).pipe(
      tap(_ => this.log(`update category id=${category.id}`)),
      catchError(this.handleError<any>('updateCategory'))
    );
  }

  /** DELETE: サーバーからCategoryを削除 */
  deleteCategory(id: number): Observable<Category> {
    this.setCsrfToken();
    const url = `${this.categoryUrl}/${id}`;

    return this.http.delete<Category>(url, this.httpOptions).pipe(
      tap(_ => this.log(`deleted Category id=${id}`)),
      catchError(this.handleError<Category>('deleteCategory'))
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

  /** CategoryServiceのメッセージをMessageServiceを使って記録 */
  private log(message: string) {
    //this.messageService.add(`CategoryService: ${message}`);
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
